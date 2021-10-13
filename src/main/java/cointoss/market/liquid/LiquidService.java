/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.liquid;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
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

public class LiquidService extends MarketService {

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(300).refresh(Duration.ofMinutes(5));

    // /** The realtime communicator. */
    // private static final EfficientWebSocket Realtime =
    // EfficientWebSocket.with.address("wss://tap.liquid.com/app/LiquidTapClient")
    // .extractId(json -> {
    // String first = json.text("0");
    // if (Character.isDigit(first.charAt(0))) {
    // return "trades-" + json.text("1");
    // } else {
    // return "orderbook-" + first;
    // }
    // })
    // .noServerReply();

    /**
     * @param marketName
     * @param setting
     */
    protected LiquidService(String marketName, MarketSetting setting) {
        super(Exchange.Coincheck, marketName, setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        // return Realtime;
        throw new Error();
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

    public static void main(String[] args) throws InterruptedException {
        Liquid.BTC_JPY.executionsRealtimely().to(e -> {
            System.out.println(e);
        });
        Thread.sleep(10000);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];

        Signal<JSON> multiples = I.signal();
        for (int i = 4; 0 <= i; i--) {
            multiples = multiples.concat(call("PRIVATE", "orders/completes?pair=" + marketName + "&last_id=" + (id - 50 * i))
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
        return null;
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
        return OrderBookPageChanges.byJSON(pages.find("bids", "*"), pages.find("asks", "*"), "0", "1");
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

        return Network.rest(builder, Limit, client()).retry(retryPolicy(10, "Coincheck RESTCall"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportHistoricalTrade() {
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