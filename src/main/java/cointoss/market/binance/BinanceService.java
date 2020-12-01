/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.Network;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BinanceService extends MarketService {

    /** The API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(550).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.binance.com:9443/stream")
            .extractId(json -> json.text("stream"));

    /** The realtime communicator. */
    private static final EfficientWebSocket RealtimeFuture = Realtime.withAddress("wss://fstream.binance.com/stream");

    /** The market type. */
    private final boolean isFutures;

    /**
     * @param marketName
     * @param setting
     */
    protected BinanceService(String marketName, boolean isFutures, MarketSetting setting) {
        super(isFutures ? Exchange.BinanceF : Exchange.Binance, marketName, setting);

        this.isFutures = isFutures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EfficientWebSocket clientRealtimely() {
        return isFutures ? RealtimeFuture : Realtime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Integer> delay() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1000&fromId=" + (startId + 1)).flatIterable(e -> e.find("*"))
                .map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return clientRealtimely().subscribe(new Topic("aggTrade", marketName)).map(json -> convert(json.get("data")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("*")).map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1&fromId=" + id).flatIterable(e -> e.find("*")).map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders(OrderState state) {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Order> connectOrdersRealtimely() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<OrderBookPageChanges> orderBook() {
        return call("GET", "depth?symbol=" + marketName + "&limit=" + (isFutures ? "1000" : "5000"))
                .map(e -> convertOrderBook(e, "bids", "asks"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("depth", marketName)).map(json -> convertOrderBook(json.get("data"), "b", "a"));
    }

    /**
     * Convert JSON to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JSON pages, String bidName, String askName) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        for (JSON bid : pages.find(bidName, "*")) {
            Num price = bid.get(Num.class, "0");
            double size = Double.parseDouble(bid.text("1"));

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JSON ask : pages.find(askName, "*")) {
            Num price = ask.get(Num.class, "0");
            double size = Double.parseDouble(ask.text("1"));

            change.asks.add(new OrderBookPage(price, size));
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal(Num.ZERO);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @return
     */
    private Execution convert(JSON e) {
        long id = Long.parseLong(e.text("a"));
        Direction side = e.get(Boolean.class, "m") ? Direction.SELL : Direction.BUY;
        Num size = e.get(Num.class, "q");
        Num price = e.get(Num.class, "p");
        ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("T")));

        return Execution.with.direction(side, size)
                .id(id)
                .price(price)
                .date(date)
                .consecutive(Execution.ConsecutiveDifference)
                .delay(Execution.DelayInestimable);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        String uri = isFutures ? "https://fapi.binance.com/fapi/v1/" : "https://api.binance.com/api/v3/";
        Builder builder = HttpRequest.newBuilder(URI.create(uri + path));

        switch (method) {
        case "GET":
            break;

        default:
            throw new IllegalArgumentException("Unexpected value: " + method);
        }
        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Binance RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        private static final AtomicInteger counter = new AtomicInteger();

        public String method = "SUBSCRIBE";

        public List<String> params = new ArrayList();

        public int id = counter.incrementAndGet();

        /** The string expression to make equality checking fast. */
        private String idText = Integer.toString(id);

        private Topic(String channel, String market) {
            super(market.toLowerCase() + "@" + channel, topic -> topic.method = "UNSUBSCRIBE");
            this.params.add(market.toLowerCase() + "@" + channel);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return reply.has("id", idText);
        }
    }
}