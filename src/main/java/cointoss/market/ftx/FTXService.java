/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Exchange;
import cointoss.market.Numbering;
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

public class FTXService extends MarketService {

    /** The idetifier management. */
    static final Numbering Numbering = new Numbering(false, 1000);

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ftx.com/ws/")
            .extractId(json -> json.has("type", "update") ? json.text("channel") + "@" + json.text("market") : null)
            .recconnectIf(json -> json.has("code", 20001))
            .stopRecconnectIf(json -> json.has("code", "400"));

    /**
     * @param marketName
     * @param setting
     */
    protected FTXService(String marketName, MarketSetting setting) {
        super(Exchange.FTX, marketName, setting);
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
    public Signal<Integer> delay() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<String> request(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> cancel(Order order) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long startId, long endId) {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        long startTime = Numbering.decode(startId) + 1;
        long[] endTime = {Numbering.decode(endId)};

        return new Signal<JSON>((observer, disposer) -> {
            int latestSize = 0;
            List<JSON> executions = new ArrayList(setting.acquirableExecutionSize);

            // Retrieve the execution history between the specified dates and times in small chunks.
            while (!disposer.isDisposed()) {
                call("GET", "markets/" + marketName + "/trades?limit=200&start_time=" + startTime + "&end_time=" + endTime[0])
                        .flatIterable(e -> e.find("result", "*"))
                        .waitForTerminate()
                        .toCollection(executions);

                int size = executions.size();
                if (latestSize == size) {
                    break;
                } else {
                    latestSize = size;
                    endTime[0] = Numbering.decode(Numbering.encode(parseTime(executions.get(size - 1).text("time"))));
                }
            }

            // Since the first one is the most recent value, it is sent in chronological order,
            // starting with the last one.
            for (int i = latestSize - 1; 0 <= i; i--) {
                observer.accept(executions.get(i));
            }
            observer.complete();

            return disposer;
        }).map(json -> convert(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("trades", marketName))
                .flatIterable(json -> json.find("data", "*"))
                .map(json -> convert(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "markets/" + marketName + "/trades?limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return call("GET", "markets/" + marketName + "/trades?limit=1&end_time=" + Numbering.decode(id))
                .flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateAcquirableExecutionIdRange(double factor) {
        return Math.round(factor) * Numbering.padding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return one.buyer.equals(other.buyer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Order> orders() {
        return I.signal();
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
        return call("GET", "markets/" + marketName + "/orderbook?depth=100").map(json -> json.get("result")).map(this::convertOrderBook);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderbook", marketName)).map(json -> convertOrderBook(json.get("data")));
    }

    /**
     * Convert JSON to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JSON pages) {
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
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return null;
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e, AtomicLong increment, Object[] previous) {
        Direction side = e.get(Direction.class, "side");
        Num size = e.get(Num.class, "size");
        Num price = e.get(Num.class, "price");
        ZonedDateTime date = parseTime(e.text("time"));
        boolean liquidation = e.get(Boolean.class, "liquidation");
        long id;
        int consecutive;

        if (date.equals(previous[1])) {
            id = Numbering.encode(date) + increment.incrementAndGet();

            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = Numbering.encode(date);
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size)
                .id(id)
                .price(price)
                .date(date)
                .consecutive(consecutive)
                .delay(liquidation ? Execution.DelayHuge : Execution.DelayInestimable)
                .buyer(e.text("id"));
    }

    /**
     * Parse time format.
     * 
     * @param time
     * @return
     */
    private ZonedDateTime parseTime(String time) {
        switch (time.length()) {
        case 32: // 2019-10-04T06:06:21.353533+00:00
            time = time.substring(0, 26);
            break;

        case 25: // 2019-10-04T06:07:30+00:00
            time = time.substring(0, 19).concat(".000000");
            break;

        default:
            throw new IllegalArgumentException("Unexpected value: " + time);
        }
        return LocalDateTime.parse(time, TimeFormat).atZone(Chrono.UTC);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://ftx.com/api/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "FTX RESTCall"));
    }

    /**
     * 
     */
    static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public String channel;

        public String market;

        /**
         * @param channel
         * @param market
         */
        private Topic(String channel, String market) {
            super(channel + "@" + market, topic -> topic.op = "unsubscribe");

            this.channel = channel;
            this.market = market;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            return "subscribed".equals(reply.text("type")) && channel.equals(reply.text("channel")) && market.equals(reply.text("market"));
        }
    }
}