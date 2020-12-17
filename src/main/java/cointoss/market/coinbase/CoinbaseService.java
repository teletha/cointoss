/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.coinbase;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.TimestampBasedMarketServiceSupporter;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.util.APILimiter;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.JSON;
import kiss.Signal;

public class CoinbaseService extends MarketService {

    private static final DateTimeFormatter TimeFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]X");

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(8).refresh(500, MILLISECONDS);

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ws-feed.pro.coinbase.com")
            .extractId(json -> json.text("type") + ":" + json.text("product_id"));

    /**
     * @param marketName
     * @param setting
     */
    protected CoinbaseService(String marketName, MarketSetting setting) {
        super(Exchange.Coinbase, marketName, setting);
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

        return call("GET", "products/" + marketName + "/trades?before=" + startId + "&after=" + (endId + 1)).flatIterable(e -> e.find("*"))
                .reverse()
                .map(json -> createExecution(json, false, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        long[] previous = new long[3];

        return clientRealtimely().subscribe(new Topic("ticker", marketName)).map(json -> createExecution(json, true, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "products/" + marketName + "/trades?limit=1").flatIterable(e -> e.find("*"))
                .map(json -> createExecution(json, false, new long[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsBefore(long id) {
        long[] context = new long[3];

        return call("GET", "products/" + marketName + "/trades?after=" + id).flatIterable(e -> e.find("*"))
                .reverse()
                .map(json -> createExecution(json, false, context));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution createExecution(JSON e, boolean realtime, long[] previous) {
        long id = e.get(long.class, "trade_id");
        Direction side = e.get(Direction.class, "side");
        if (!realtime) side = side.inverse();
        Num size = e.get(Num.class, realtime ? "last_size" : "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);
        int consecutive = TimestampBasedMarketServiceSupporter.computeConsecutive(side, date.toInstant().toEpochMilli(), previous);

        return Execution.with.direction(side, size).price(price).id(id).date(date).consecutive(consecutive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "products/" + marketName + "/book?level=2").map(root -> {
            OrderBookPageChanges changes = new OrderBookPageChanges();

            for (JSON ask : root.find("asks", "*")) {
                Num price = ask.get(Num.class, "0");
                double size = ask.get(double.class, "1");
                changes.asks.add(new OrderBookPage(price, size));
            }
            for (JSON bid : root.find("bids", "*")) {
                Num price = bid.get(Num.class, "0");
                double size = bid.get(double.class, "1");
                changes.bids.add(new OrderBookPage(price, size));
            }

            return changes;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> createOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("level2", marketName)).map(root -> {
            OrderBookPageChanges changes = new OrderBookPageChanges();

            for (JSON ask : root.find("changes", "*")) {
                Direction side = ask.get(Direction.class, "0");
                Num price = ask.get(Num.class, "1");
                double size = ask.get(double.class, "2");
                if (side == Direction.BUY) {
                    changes.bids.add(new OrderBookPage(price, size));
                } else {
                    changes.asks.add(new OrderBookPage(price, size));
                }
            }

            return changes;
        });
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.pro.coinbase.com/" + path));

        return Network.rest(builder, LIMITER, client()).retryWhen(retryPolicy(10, exchange + " RESTCall"));
    }

    /**
     * Subscription topic for websocket communication.
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String type = "subscribe";

        public List<String> product_ids = new ArrayList();

        public List<String> channels = new ArrayList();

        private Topic(String channel, String market) {
            super((channel.equals("level2") ? "l2update" : channel) + ":" + market, topic -> topic.type = "unsubscribe");

            product_ids.add(market);
            channels.add(channel);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.text("type").equals("subscriptions") && reply.find(String.class, "channels", "*", "name")
                    .contains(channels.get(0));
        }
    }
}