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

    /** The market id. */
    private final int marketId;

    /** The instrument tick size. */
    private final Num instrumentTickSize;

    /** The shared websocket connection. */
    private Signal<JSON> websocket;

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

        return connectSharedWebSocket(Topic.trade).flatIterable(e -> e).map(e -> convert(e, increment, previous));
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
        return connectSharedWebSocket(Topic.orderBookL2).map(this::convertOrderBook);
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(List<JSON> array) {
        OrderBookPageChanges change = new OrderBookPageChanges();
        for (JSON o : array) {
            long id = o.getAs(Long.class, "id");
            Num price = instrumentTickSize.multiply((100000000L * marketId) - id);
            JSON sizeElement = o.get("size");
            double size = sizeElement == null ? 0 : sizeElement.to(Double.class) / price.doubleValue();

            if (o.getAs(String.class, "side").charAt(0) == 'B') {
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
        Direction direction = Direction.parse(e.getAs(String.class, "side"));
        Num size = Num.of(e.getAs(String.class, "homeNotional"));
        Num price = Num.of(e.getAs(String.class, "price"));
        ZonedDateTime date = ZonedDateTime.parse(e.getAs(String.class, "timestamp"), RealTimeFormat).withZoneSameLocal(Chrono.UTC);
        String tradeId = e.getAs(String.class, "trdMatchID");
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

        return network.rest2(request, Limit).retryWhen(retryPolicy(10, "BitMEX RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<List<JSON>> connectSharedWebSocket(Topic topic) {
        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.op = "subscribe";
            for (Topic type : Topic.values()) {
                command.args.add(type + ":" + marketName);
            }

            websocket = network.websocket2("wss://www.bitmex.com/realtime", command);
        }

        return websocket.share().flatMap(root -> {
            if (root.has("table", topic.name())) {
                return I.signal(root.get("data")).map(e -> e.find("*"));
            } else {
                return I.signal();
            }
        }).effectOnError(e -> {
            e.printStackTrace();
        });
    }

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        trade, orderBookL2, orderBookL2_25;
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String op;

        public List<String> args = new ArrayList();
    }
}