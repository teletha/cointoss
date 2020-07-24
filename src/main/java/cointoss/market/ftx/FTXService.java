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
import java.util.concurrent.atomic.AtomicLong;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.TimeBasedId;
import cointoss.order.Order;
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

public class FTXService extends MarketService {

    /** The id manager. */
    static final TimeBasedId ID = new TimeBasedId(1000);

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(20).refresh(Duration.ofSeconds(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = EfficientWebSocket.with.address("wss://ftx.com/ws/")
            .extractId(json -> json.has("type", "update") ? json.text("channel") + "@" + json.text("market") : null);

    /**
     * @param marketName
     * @param setting
     */
    protected FTXService(String marketName, MarketSetting setting) {
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

        double coefficient = (endId - startId) / (double) setting.acquirableExecutionSize;
        long startTime = ID.secs(startId);
        long endTime = startTime + (long) (2 * coefficient);
        return call("GET", "markets/" + marketName + "/trades?limit=200&start_time=" + startTime + "&end_time=" + endTime)
                .flatIterable(e -> e.find("result", "*"))
                .reverse()
                .map(json -> convert(json, increment, previous));
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
    public long estimateInitialExecutionId() {
        return 1569888000000L * ID.padding;
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
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return I.signal();
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
        boolean liquidation = e.get(Boolean.class, "liquidation");

        String time = e.text("time");
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
        ZonedDateTime date = LocalDateTime.parse(time, TimeFormat).atZone(Chrono.UTC);
        long id;
        int consecutive;

        if (date.equals(previous[1])) {
            id = ID.decode(date) + increment.incrementAndGet();

            if (side != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (side == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = ID.decode(date);
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
                .delay(liquidation ? Execution.DelayHuge : Execution.DelayInestimable);
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
        System.out.println(builder.build());

        return Network.rest(builder, Limit, client()).retryWhen(retryPolicy(10, "FTX RESTCall"));
    }

    /**
     * 
     */
    private static class Topic extends IdentifiableTopic<Topic> {

        public String op = "subscribe";

        public String channel;

        public String market;

        /**
         * 
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

    public static void main(String[] args) throws InterruptedException {
        // https://ftx.com/api/markets/BTC-PERP/trades?limit=200&start_time=1570944652&end_time=1570944660
        // GET
        // https://ftx.com/api/markets/BTC-PERP/trades?limit=200&start_time=1570944652&end_time=1570944654
        // GET
        // 2020-07-23 13:55:25.520 INFO REST write on FTX BTC-PERP from
        // 2019-10-13T05:30:52.304052Z[UTC]. size 2 (1)
        //
        // FTX.BTC_USD.executions(1570944654, 1570944660).to(e -> {
        // System.out.println(e);
        // });

        Market market = new Market(FTX.BTC_USD);
        market.readLog(log -> log.fromYestaday());

        // FTX.BTC_USD.executionsRealtimely().to(e -> {
        // System.out.println(e);
        // });

        Thread.sleep(1000 * 5);
    }

}