/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;

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
import cointoss.order.Order;
import cointoss.order.OrderBookPage;
import cointoss.order.OrderBookPageChanges;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import cointoss.util.EfficientWebSocket.IdentifiableTopic;
import cointoss.util.Num;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import okhttp3.Request;

class BitMexService extends MarketService {

    /** The right padding for id. */
    private static final long PaddingForID = 100000;

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(45).refresh(Duration.ofMinutes(1));

    /** The realtime communicator. */
    private static final EfficientWebSocket Realtime = new EfficientWebSocket("wss://www.bitmex.com/realtime", 25, json -> json
            .text("table") + json.find(String.class, "data", "0", "symbol"));

    /** The market id. */
    private final int marketId;

    /** The instrument tick size. */
    private final Num instrumentTickSize;

    /**
     * @param marketName
     * @param setting
     */
    BitMexService(int id, String marketName, MarketSetting setting) {
        super("BitMEX", marketName, setting);

        this.marketId = id;
        this.instrumentTickSize = marketName.equals("XBTUSD") ? Num.of("0.01") : setting.baseCurrencyMinimumBidPrice;
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
    public Signal<Execution> executions(long start, long end) {
        start++;
        long startingPoint = start % PaddingForID;
        AtomicLong increment = new AtomicLong(startingPoint - 1);
        Object[] previous = new Object[] {null, encodeId(start)};

        return call("GET", "trade?symbol=" + marketName + "&count=1000" + "&startTime=" + formatEncodedId(start) + "&start=" + startingPoint)
                .flatIterable(e -> e.find("*"))
                .map(json -> {
                    return convert(json, increment, previous);
                });
    }

    private ZonedDateTime encodeId(long id) {
        return Chrono.utcByMills(id / PaddingForID);
    }

    private String formatEncodedId(long id) {
        return RealTimeFormat.format(encodeId(id));
    }

    private long decodeId(ZonedDateTime time) {
        return time.toInstant().toEpochMilli() * PaddingForID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return Realtime.subscribe(new Topic("trade", marketName))
                .flatIterable(json -> json.find("data", "*"))
                .map(json -> convert(json, increment, previous));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "trade?symbol=" + marketName + "&count=1&reverse=true").flatIterable(e -> e.find("*"))
                .map(json -> convert(json, new AtomicLong(), new Object[2]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return decodeId(Chrono.utc(2020, 1, 1).minusMinutes(3));
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
        return call("GET", "orderBook/L2?depth=1200&symbol=" + marketName).map(e -> convertOrderBook(e.find("*")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        return Realtime.subscribe(new Topic("orderBookL2", marketName)).map(json -> json.find("data", "*")).map(this::convertOrderBook);
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param pages
     * @return
     */
    private OrderBookPageChanges convertOrderBook(List<JSON> pages) {
        OrderBookPageChanges change = new OrderBookPageChanges();
        for (JSON page : pages) {
            long id = Long.parseLong(page.text("id"));
            Num price = instrumentTickSize.multiply((100000000L * marketId) - id);
            JSON sizeElement = page.get("size");
            double size = sizeElement == null ? 0 : sizeElement.as(Double.class) / price.doubleValue();

            if (page.text("side").charAt(0) == 'B') {
                change.bids.add(new OrderBookPage(price, size));
            } else {
                change.asks.add(new OrderBookPage(price, size));
            }
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
     * {@inheritDoc}
     */
    @Override
    public boolean checkEquality(Execution one, Execution other) {
        return one.buyer.equals(other.buyer);
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JSON e, AtomicLong increment, Object[] previous) {
        Direction direction = Direction.parse(e.get(String.class, "side"));
        Num size = Num.of(e.get(String.class, "homeNotional"));
        Num price = Num.of(e.get(String.class, "price"));
        ZonedDateTime date = ZonedDateTime.parse(e.get(String.class, "timestamp"), RealTimeFormat).withZoneSameLocal(Chrono.UTC);
        String tradeId = e.get(String.class, "trdMatchID");
        long id;
        int consecutive;

        if (date.equals(previous[1])) {
            id = decodeId(date) + increment.incrementAndGet();

            if (direction != previous[0]) {
                consecutive = Execution.ConsecutiveDifference;
            } else if (direction == Direction.BUY) {
                consecutive = Execution.ConsecutiveSameBuyer;
            } else {
                consecutive = Execution.ConsecutiveSameSeller;
            }
        } else {
            id = decodeId(date);
            increment.set(0);
            consecutive = Execution.ConsecutiveDifference;
        }

        previous[0] = direction;
        previous[1] = date;

        return Execution.with.direction(direction, size).id(id).price(price).date(date).consecutive(consecutive).buyer(tradeId);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JSON> call(String method, String path) {
        Request request = new Request.Builder().url("https://www.bitmex.com/api/v1/" + path).build();

        return network.rest(request, Limit).retryWhen(retryPolicy(10, "BitMEX RESTCall"));
    }

    /**
     * Subscription topic for websocket communication.
     */
    public static class Topic extends IdentifiableTopic {

        public String op = "subscribe";

        public List<String> args = new ArrayList();

        private Topic(String channel, String market) {
            super(channel + "[" + market + "]");

            this.args.add(channel + ":" + market);
        }
    }
}