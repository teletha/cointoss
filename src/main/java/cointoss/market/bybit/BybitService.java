/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bybit;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.LogHouse;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.orderbook.OrderBookPage;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import hypatia.Num;
import kiss.JSON;
import kiss.Signal;

public class BybitService extends MarketService {

    static final TimestampBasedMarketServiceSupporter Support = new TimestampBasedMarketServiceSupporter();

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.bybit.com/v5/public/spot")
            .extractId(json -> json.text("topic"));

    /**
     * @param marketName
     * @param setting
     */
    protected BybitService(String marketName, MarketSetting setting) {
        super(Exchange.Bybit, marketName, setting);
        this.executionRequestLimit = 1000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return Realtime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        long[] context = new long[3];

        return call("GET", "market/recent-trade?symbol=" + marketName + "&category=linear&limit=1000")
                .flatIterable(e -> e.find("result", "list", "*"))
                .map(e -> {
                    Direction side = e.get(Direction.class, "side");
                    Num price = e.get(Num.class, "price");
                    Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
                    ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "time"));

                    return Support.createExecution(side, size, price, date, context);
                })
                .take(e -> startId <= e.id && e.id <= endId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("publicTrade", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            Direction side = e.get(Direction.class, "S");
            Num price = e.get(Num.class, "p");
            Num size = e.get(Num.class, "v").divide(price).scale(setting.target.scale);
            ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("T")));

            return Support.createExecution(side, size, price, date, context);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "market/recent-trade?symbol=" + marketName + "&category=linear&limit=1")
                .flatIterable(e -> e.find("result", "list", "*"))
                .map(json -> convert(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param context
     * @return
     */
    private Execution convert(JSON e, long[] context) {
        Direction side = e.get(Direction.class, "side");
        Num price = e.get(Num.class, "price");
        Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
        ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "time"));

        return Support.createExecution(side, size, price, date, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return other.id <= one.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "market/orderbook?category=" + category() + "&symbol=" + marketName + "&limit=200").map(pages -> {
            JSON result = pages.get("result");
            return convertOrderBook(result.find("a", "*"), result.find("b", "*"));
        });
    }

    private String category() {
        if (setting.type.isDerivative() || setting.type.isFuture()) {
            return "linear";
        } else if (setting.type.isSpot()) {
            return "spot";
        } else {
            throw new Error("Unspported type [" + setting.type + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook.200", marketName)).map(pages -> {
            JSON data = pages.get("data");
            return convertOrderBook(data.find("a", "*"), data.find("b", "*"));
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Bybit.BTC_USDT.orderBookRealtimely().to(x -> {
            System.out.println(x.bestBid());
        }, e -> {
            e.printStackTrace();
        });

        Thread.sleep(1000 * 30);
    }

    /**
     * Convert to {@link OrderBookPage}.
     * 
     * @param changes
     * @param e
     */
    private OrderBookChanges convertOrderBook(List<JSON> asks, List<JSON> bids) {
        return OrderBookChanges.byJSON(bids, asks, "0", "1");
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // protected FeatherStore<OpenInterest> initializeOpenInterest() {
    // return FeatherStore.create(OpenInterest.class, Span.Minute5)
    // .enableDiskStore(file("oi.db"))
    // .enableDataSupplier(time -> provideOpenInterest(Chrono.utcBySeconds(time)),
    // connectOpenInterest());
    // }
    //
    // /**
    // * @param startExcluded
    // * @return
    // */
    // private Signal<OpenInterest> provideOpenInterest(ZonedDateTime startExcluded) {
    // return call("GET", "market/open-interest?symbol=" + marketName +
    // "&category=linear&intervalTime=5min&limit=200")
    // .flatIterable(e -> e.find("result", "list", "$"))
    // .map(e -> {
    // float size = e.get(float.class, "openInterest");
    // ZonedDateTime date = Chrono.utcByMills(e.get(long.class, "timestamp"));
    // OpenInterest oi = OpenInterest.with.date(date).size(size);
    // return oi;
    // })
    // .take(e -> e.date.isAfter(startExcluded));
    // }
    //
    // /**
    // * @return
    // */
    // private Signal<OpenInterest> connectOpenInterest() {
    // return clientRealtimely().subscribe(new Topic("tickers", marketName)).map(e ->
    // e.get("data")).map(e -> {
    // if (e.has("openInterest")) {
    // return OpenInterest.with.date(Chrono.utcNow()).size(e.get(float.class, "openInterest"));
    // } else {
    // return (OpenInterest) null;
    // }
    // }).skipNull();
    // }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.bybit.com/v5/" + path));

        return Network.rest(builder, Limit, client()).retry(withPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogHouse loghouse() {
        return new OfficialLogHouse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportStableExecutionQuery() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportExternalLogHouse() {
        return true;
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "." + market);
            args.add(channel + "." + market);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.text("success").equals("true") && reply.text("op").equals("subscribe");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.op = "unsubscribe";
        }
    }
}