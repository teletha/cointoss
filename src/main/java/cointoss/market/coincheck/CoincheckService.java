/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coincheck;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.orderbook.OrderBookChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class CoincheckService extends MarketService {

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(4).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ws-api.coincheck.com/").extractId(json -> {
        String first = json.text("0");
        if (first.isEmpty()) {
            return "trades-" + json.get("0").text("2");
        } else {
            return "orderbook-" + first;
        }
    }).noServerReply();

    /**
     * @param marketName
     * @param setting
     */
    protected CoincheckService(String marketName, MarketSetting setting) {
        super(Exchange.Coincheck, marketName, setting);
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
    protected Signal<Execution> connectExecutionRealtimely() {
        return clientRealtimely().subscribe(new Topic("trades", marketName)).flatIterable(e -> e.find("$")).map(e -> {
            ZonedDateTime date = Chrono.utcBySeconds(e.get(long.class, "0"));
            long id = e.get(long.class, "1");
            Num price = e.get(Num.class, "3");
            Num size = e.get(Num.class, "4");
            Direction side = e.get(Direction.class, "5");

            return Execution.with.direction(side, size).price(price).id(id).date(date);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsAfter(long startId, long endId) {
        long[] context = new long[3];

        return this.call("GET", "trades?pair=" + marketName + "&limit=100")
                .flatIterable(e -> e.find("data", "*"))
                .map(json -> createExecution(json, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trades?pair=" + marketName + "&limit=1").flatIterable(e -> e.find("data", "*"))
                .map(json -> createExecution(json, new long[3]));
    }

    public static void main(String[] args) {
        I.load(Market.class);

        // Coincheck.BTC_JPY.executionsRealtimely().waitForTerminate().to(x -> {
        // System.out.println(x);
        // });

        // Coincheck.BTC_JPY.executions(10, 20).waitForTerminate().to(x -> {
        // System.out.println(x);
        // });

        // Market market = Market.of(Coincheck.BTC_JPY);
        // market.readLog(x -> x.fromToday(LogType.Fast));
        //
        // try {
        // Thread.sleep(1000 * 60 * 20);
        // } catch (InterruptedException e) {
        // throw I.quiet(e);
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];

        Signal<JSON> multiples = I.signal();
        for (int i = 4; 0 <= i; i--) {
            multiples = multiples.concat(call("GET", "trades?pair=" + marketName + "&ending_beforeID=" + (id - 50 * i))
                    .flatIterable(e -> e.find("completes", "$")));
        }

        return multiples.map(json -> createExecution(json, context));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param context
     * @return
     */
    private Execution createExecution(JSON e, long[] context) {
        Direction side = e.get(Direction.class, "order_type");
        Num size = e.get(Num.class, "amount");
        Num price = e.get(Num.class, "rate");
        ZonedDateTime date = ZonedDateTime.parse(e.text("created_at"), TimeFormat);
        long id = e.get(long.class, "id");

        return Execution.with.direction(side, size).price(price).id(id).date(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookChanges> orderBook() {
        return call("GET", "order_books?pair=" + marketName).map(this::createOrderBook);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook", marketName)).map(json -> createOrderBook(json.get("1")));
    }

    /**
     * Convert JSON to {@link OrderBookChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookChanges createOrderBook(JSON pages) {
        return OrderBookChanges.byJSON(pages.find("bids", "*"), pages.find("asks", "*"), "0", "1");
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder;
        if (method.equals("PRIVATE")) {
            builder = HttpRequest.newBuilder(URI.create("https://coincheck.com/exchange/" + path));
        } else {
            builder = HttpRequest.newBuilder(URI.create("https://coincheck.com/api/" + path));
        }

        return Network.rest(builder, Limit, client()).retry(withPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportStableExecutionQuery() {
        return false;
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String type = "subscribe";

        public String channel;

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "-" + market);

            this.channel = market + "-" + channel;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildUnsubscribeMessage(Topic topic) {
            topic.type = "unsubscribe";
        }
    }
}