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
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.Consecutive;
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
import cointoss.util.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class BybitService extends MarketService {

    /** The idetifier management. */
    static final Numbering Numbering = new Numbering(false, 1000);

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
        super("FTX", marketName, setting);
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
                }
            }

            // Since the first one is the most recent value, it is sent in chronological order,
            // starting with the last one.
            for (int i = latestSize - 1; 0 <= i; i--) {
                observer.accept(executions.get(i));
            }
            observer.complete();

            return disposer;
        }).map(json -> convertByREST(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong counter = new AtomicLong(-1);
        Consecutive consecutive = new Consecutive();

        return clientRealtimely().subscribe(new Topic("trade", marketName)).flatIterable(json -> json.find("data", "*")).map(e -> {
            long id = counter.updateAndGet(now -> now == -1 ? calculateId(e) : now + 1);
            Direction side = e.get(Direction.class, "side");
            Num price = e.get(Num.class, "price");
            Num size = e.get(Num.class, "size").divide(price).scale(setting.target.scale);
            ZonedDateTime date = Chrono.utcByMills(Long.parseLong(e.text("trade_time_ms")));

            return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive.compute(side, date));
        });
    }

    /**
     * Calculate the actual id.
     * 
     * @param exe The target execution data.
     * @return An actual id.
     */
    private synchronized long calculateId(JSON exe) {
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
                .map(json -> convertByREST(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatestAt(long id) {
        return call("GET", "trading-records?symbol=" + marketName).flatIterable(e -> e.find("result", "*"))
                .map(json -> convertByREST(json, new AtomicLong(), new Object[2]));
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
    private Execution convertByREST(JSON e, AtomicLong increment, Object[] previous) {
        System.out.println(e);
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
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = side;
        previous[1] = date;

        return Execution.with.direction(side, size).id(id).price(price).date(date).consecutive(consecutive);
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

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "FTX RESTCall"));
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

    public static void main(String[] args) throws InterruptedException {
        Bybit.BTC_USD.executionsRealtimely().to(e -> {
            System.out.println(e);
        });

        Thread.sleep(30 * 1000);
    }
}