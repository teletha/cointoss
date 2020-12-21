/*
 * Copyright (C) 2020 cointoss Development Team
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
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
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
        if (Character.isDigit(first.charAt(0))) {
            return "trades-" + json.text("1");
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
        return clientRealtimely().subscribe(new Topic("trades", marketName)).map(e -> {
            Direction side = e.get(Direction.class, "4");
            Num size = e.get(Num.class, "3");
            Num price = e.get(Num.class, "2");
            ZonedDateTime date = Chrono.utcNow();
            long id = e.get(long.class, "0");

            return Execution.with.direction(side, size).price(price).id(id).date(date);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        long[] context = new long[3];

        return this.call("PRIVATE", "orders/completes?pair=" + marketName + "&last_id=" + (startId + 52))
                .flatIterable(e -> e.find("completes", "$"))
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

    public static final void main(String[] a) throws InterruptedException {
        // Market m = new Market(Coincheck.BTC_JPY);
        // m.readLog(x -> x.fromYestaday());
        Coincheck.BTC_JPY.executionsBefore(100000).waitForTerminate().to(e -> {
            System.out.println(e);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];

        Signal<JSON> multiples = I.signal();
        for (int i = 0; i < 10; i++) {
            multiples = multiples.concat(call("PRIVATE", "orders/completes?pair=" + marketName + "&last_id=" + (id + 1 + 50 * i))
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
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "order_books?pair=" + marketName).map(this::createOrderBook);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook", marketName)).map(json -> createOrderBook(json.get("1")));
    }

    /**
     * Convert JSON to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges createOrderBook(JSON pages) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        for (JSON bid : pages.find("bids", "*")) {
            Num price = bid.get(Num.class, "0");
            double size = Double.parseDouble(bid.text("1"));

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JSON ask : pages.find("asks", "*")) {
            Num price = ask.get(Num.class, "0");
            double size = Double.parseDouble(ask.text("1"));

            change.asks.add(new OrderBookPage(price, size));
        }
        return change;
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

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Coincheck RESTCall"));
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
            super(channel + "-" + market, topic -> topic.type = "unsubscribe");

            this.channel = market + "-" + channel;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return false;
        }
    }
}