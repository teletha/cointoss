/*
 * Copyright (C) 2019 CoinToss Development Team
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBoard;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import okhttp3.Request;

class BitMexService extends MarketService {

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(60).refresh(Duration.ofMinutes(1));

    /** The market id. */
    private final int id;

    /** The instrument tick size. */
    private final Num instrumentTickSize;

    /**
     * @param marketName
     * @param setting
     */
    BitMexService(int id, String marketName, MarketSetting setting) {
        super("BitMEX", marketName, setting);

        this.id = id;
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

    private static final long padding = 100000;

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executions(long start, long end) {
        start++;
        long startingPoint = start % padding;
        AtomicLong increment = new AtomicLong(startingPoint - 1);
        Object[] previous = new Object[] {null, encodeId(start)};

        return call("GET", "trade?symbol=" + marketName + "&count=1000" + "&startTime=" + formatEncodedId(start) + "&start=" + startingPoint)
                .flatIterable(JsonElement::getAsJsonArray)
                .map(json -> {
                    return convert(json, increment, previous);
                });
    }

    private ZonedDateTime encodeId(long id) {
        return Chrono.utcByMills(id / padding);
    }

    private String formatEncodedId(long id) {
        return RealTimeFormat.format(encodeId(id));
    }

    private long decodeId(ZonedDateTime time) {
        return time.toInstant().toEpochMilli() * padding;
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
        return call("GET", "/trade?symbol=" + marketName + "&count=1&reverse=true").flatIterable(JsonElement::getAsJsonArray)
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
    public Signal<OrderBookChange> orderBook() {
        return call("GET", "orderBook/L2?depth=400&symbol=" + marketName).map(this::convertOrderBook);
    }

    private OrderBookChange convertOrderBook(JsonElement e) {
        OrderBookChange change = new OrderBookChange();
        for (JsonElement element : e.getAsJsonArray()) {
            JsonObject o = element.getAsJsonObject();
            Num price = Num.of(o.get("price").getAsString());
            Num size = Num.of(o.get("size").getAsString());
            size = size.divide(price);

            if (o.get("side").getAsString().equals("Buy")) {
                change.bids.add(new OrderBoard(price, size.doubleValue()));
            } else {
                change.asks.add(new OrderBoard(price, size.doubleValue()));
            }
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookChange> connectOrderBookRealtimely() {
        return connectSharedWebSocket(Topic.orderBookL2_25).map(x -> {
            OrderBookChange change = new OrderBookChange();
            for (JsonElement e : x) {
                JsonObject o = e.getAsJsonObject();
                long id = o.get("id").getAsLong();
                Num price = instrumentTickSize.multiply((100000000L * this.id) - id);
                JsonElement sizeElement = o.get("size");
                double size = sizeElement == null ? 0 : sizeElement.getAsDouble() / price.doubleValue();

                if (o.get("side").getAsString().charAt(0) == 'B') {
                    change.bids.add(new OrderBoard(price, size));
                } else {
                    change.asks.add(new OrderBoard(price, size));
                }
            }
            return change;
        });
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
    private Execution convert(JsonElement json, AtomicLong increment, Object[] previous) {
        JsonObject e = json.getAsJsonObject();

        Direction direction = Direction.parse(e.get("side").getAsString());
        Num size = Num.of(e.get("homeNotional").getAsString());
        Num price = Num.of(e.get("price").getAsString());
        ZonedDateTime date = ZonedDateTime.parse(e.get("timestamp").getAsString(), RealTimeFormat).withZoneSameLocal(Chrono.UTC);
        String tradeId = e.get("trdMatchID").getAsString();
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
    private Signal<JsonElement> call(String method, String path) {
        Request request = new Request.Builder().url("https://www.bitmex.com/api/v1/" + path).build();

        return network.rest(request, Limit);
    }

    /** The shared websocket. */
    private Signal<JsonElement> websocket;

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JsonArray> connectSharedWebSocket(Topic topic) {
        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.op = "subscribe";
            for (Topic type : Topic.values()) {
                command.args.add(type + ":" + marketName);
            }

            websocket = network.websocket("wss://www.bitmex.com/realtime", command);
        }

        return websocket.share().flatMap(e -> {
            JsonObject root = e.getAsJsonObject();
            JsonElement tableProperty = root.get("table");
            if (tableProperty == null || !tableProperty.getAsString().equals(topic.name())) {
                return I.signal();
            } else {
                return I.signal(root.get("data")).map(JsonElement::getAsJsonArray);
            }
        });
    }

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        trade, orderBookL2_25;
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String op;

        public List<String> args = new ArrayList();
    }

    public static void main(String[] args) {
        BitMexService s = (BitMexService) BitMex.XBT_USD;
        s.connectSharedWebSocket(Topic.orderBookL2_25).to(e -> {
            System.out.println(e);
        });
    }
}
