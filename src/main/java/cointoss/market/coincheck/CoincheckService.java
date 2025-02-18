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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.orderbook.OrderBookChanges;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.RateLimiter;
import hypatia.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class CoincheckService extends MarketService {

    /** The execution support. */
    private static final TimestampBasedMarketServiceSupporter SUPPORT = new TimestampBasedMarketServiceSupporter();

    /** The bitflyer API limit. */
    private static final RateLimiter LIMIT = RateLimiter.with.limit(4).refreshSecond(1).persistable(Exchange.Coincheck);

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

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

        executionMinRequest = 4;
        executionMaxRequest = 300;
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
    public Signal<Execution> executionLatest() {
        return call("GET", "trades?pair=" + marketName + "&limit=1").flatIterable(e -> e.find("data", "*"))
                .map(json -> createExecution(json, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsAfter(long startId, long endId) {
        System.out.println("latest");
        return SUPPORT.executionsAfterByCandle(startId, endId, executionMaxRequest, this::convertFromCandle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        System.out.println("before");
        return SUPPORT.executionsBeforeByCandle(id, this::convertFromCandle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> searchInitialExecution() {
        System.out.println("init");
        return SUPPORT.searchInitialExecutionByCandle(this::convertFromCandle);
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

        return SUPPORT.createExecution(side, size, price, date, context);
    }

    /**
     * Convert from candle to pseudo executions.
     * 
     * @param span
     * @param startMS
     * @param endMS
     * @return
     */
    private Signal<Execution> convertFromCandle(Span span, long startMS, long endMS) {
        System.out.println("candle " + span + "   " + Chrono.utcByMills(startMS) + "    " + Chrono.utcByMills(endMS));

        if (startMS >= endMS) {
            return I.signal();
        }

        ZonedDateTime end = Chrono.utcByMills(endMS);
        return call("GET", "charts/candle_rates?pair=" + marketName + "&unit=" + span.seconds).flatIterable(json -> json.find("$"))
                .flatIterable(json -> {
                    Num open = json.get(Num.class, "1");
                    Num close = json.get(Num.class, "4");
                    Num high = json.get(Num.class, "2");
                    Num low = json.get(Num.class, "3");
                    Num volume = json.get(Num.class, "5");
                    long time = json.get(long.class, "0") * 1000;

                    return SUPPORT.createExecutions(open, high, low, close, volume, time, span);
                })
                .skip(e -> e.isAfter(end));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] context = new long[3];

        return clientRealtimely().subscribe(new Topic("trades", marketName)).flatIterable(e -> e.find("$")).map(e -> {
            ZonedDateTime date = Chrono.utcBySeconds(e.get(long.class, "0"));
            Num price = e.get(Num.class, "3");
            Num size = e.get(Num.class, "4");
            Direction side = e.get(Direction.class, "5");

            return SUPPORT.createExecution(side, size, price, date, context);
        });
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

        return Network.rest(builder, LIMIT, client()).retry(withPolicy());
    }

    public static void main(String[] args) {
        I.load(Market.class);

        // Coincheck.BTC_JPY.executionsRealtimely().to(x -> {
        // System.out.println(x);
        // });

        // Coincheck.BTC_JPY.executionsAfter(SUPPORT.computeID(Chrono.utc(2025, 1, 1)),
        // 20).waitForTerminate().to(x -> {
        // System.out.println(x);
        // });

        Coincheck.BTC_JPY.executions().to(e -> {
            System.out.println("@@@ " + e);
        });

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
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