/*
 * Copyright (C) 2020 cointoss Development Team
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocketModel.IdentifiableTopic;
import cointoss.util.arithmeric.Num;
import cointoss.util.Network;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BybitService extends MarketService {

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]X");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://stream.bybit.com/realtime")
            .extractId(json -> json.text("topic"));

    /**
     * @param marketName
     * @param setting
     */
    protected BybitService(String marketName, MarketSetting setting) {
        super("Bybit", marketName, setting);
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
        Object[] previous = new Object[2];

        return call("GET", "trading-records?symbol=" + marketName + "&from=" + startId + "&limit=" + (endId - startId))
                .flatIterable(e -> e.find("result", "*"))
                .map(e -> convert(e, previous));
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param e
     * @param previous
     * @return
     */
    private Execution convert(JSON e, Object[] previous) {
        Direction side = e.get(Direction.class, "side");
        Num price = e.get(Num.class, "price");
        Num size = e.get(Num.class, "qty").divide(price).scale(setting.target.scale);
        ZonedDateTime date = ZonedDateTime.parse(e.text("time"), TimeFormat);
        long id = Long.parseLong(e.text("id"));
        int consecutive;

        if (date.equals(previous[1])) {
            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong counter = new AtomicLong(-1);
        Object[] previous = new Object[2];

        return clientRealtimely().subscribe(new Topic("trade", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            long id = counter.updateAndGet(now -> now == -1 ? requestId(e) : now + 1);
            Direction side = e.get(Direction.class, "side");
            Num price = e.get(Num.class, "price");
            Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
            ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("trade_time_ms")));

            int consecutive;
            if (date.equals(previous[1])) {
                if (side != previous[0]) {
                    consecutive = Execution.ConsecutiveDifference;
                } else if (side == Direction.BUY) {
                    consecutive = Execution.ConsecutiveSameBuyer;
                } else {
                    consecutive = Execution.ConsecutiveSameSeller;
                }
            } else {
                consecutive = Execution.ConsecutiveDifference;
            }
            previous[0] = side;
            previous[1] = date;

            return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
        });
    }

    /**
     * Request the actual execution id.
     * 
     * @param exe The target execution data.
     * @return An actual id.
     */
    private synchronized long requestId(JSON exe) {
        String side = exe.text("side");
        String size = exe.text("size");
        String price = exe.text("price");
        long time = Long.parseLong(exe.text("trade_time_ms"));

        List<JSON> list = call("GET", "trading-records?symbol=" + marketName + "&limit=1000").waitForTerminate()
                .flatIterable(e -> e.find("result", "*"))
                .toList();

        for (JSON item : list) {
            if (!item.text("side").equals(side)) {
                continue;
            }

            if (!item.text("qty").equals(size)) {
                continue;
            }

            if (!item.text("price").equals(price)) {
                continue;
            }

            if (ZonedDateTime.parse(item.text("time"), TimeFormat).toInstant().toEpochMilli() != time) {
                continue;
            }
            return Long.parseLong(item.text("id"));
        }
        return Long.parseLong(list.get(0).text("id")) + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trading-records?symbol=" + marketName + "&limit=1").flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return call("GET", "trading-records?symbol=" + marketName + "&from=" + id).flatIterable(e -> e.find("result", "*"))
                .map(json -> convert(json, new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return call("GET", "trading-records?symbol=" + marketName + "&from=1&limit=1").flatIterable(e -> e.find("result", "*"))
                .first()
                .waitForTerminate()
                .map(json -> Long.parseLong(json.text("id")))
                .to().v;
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
        return call("GET", "orderBook/L2?symbol=" + marketName).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();
            pages.find("result", "*").forEach(e -> convertOrderBook(change, e));
            return change;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return clientRealtimely().subscribe(new Topic("orderBook_200.100ms", marketName)).map(pages -> {
            OrderBookPageChanges change = new OrderBookPageChanges();

            String type = pages.text("type");
            if (type.equals("snapshot")) {
                pages.find("data", "*").forEach(e -> convertOrderBook(change, e));
            } else {
                pages.find("data", "delete", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "update", "*").forEach(e -> convertOrderBook(change, e));
                pages.find("data", "insert", "*").forEach(e -> convertOrderBook(change, e));
            }

            return change;
        });
    }

    /**
     * Convert to {@link OrderBookPage}.
     * 
     * @param changes
     * @param e
     */
    private void convertOrderBook(OrderBookPageChanges changes, JSON e) {
        Num price = e.get(Num.class, "price");
        String sizeValue = e.text("size");
        double size = sizeValue == null ? 0 : Double.parseDouble(sizeValue) / price.doubleValue();

        List<OrderBookPage> books = e.text("side").charAt(0) == 'B' ? changes.bids : changes.asks;
        books.add(new OrderBookPage(price, size));
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
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Builder builder = HttpRequest.newBuilder(URI.create("https://api.bybit.com/v2/public/" + path));

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "Bybit RESTCall"));
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
            super(channel + "." + market, topic -> topic.op = "unsubscribe");
            args.add(channel + "." + market);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean verifySubscribedReply(JSON reply) {
            if (reply.text("success").equals("true")) {
                JSON req = reply.get("request");
                if (req.text("op").equals("subscribe")) {
                    List<JSON> channel = req.find("args", "0");
                    if (channel.size() == 1) {
                        return channel.get(0).as(String.class).equals(args.get(0));
                    }
                }
            }
            return false;
        }
    }
}