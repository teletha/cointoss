/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.ftx;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import kiss.Signal;
import okhttp3.Request;

class FTXService extends MarketService {

    /** The right padding for id. */
    private static final long PaddingForID = 100000;

    /** The realtime data format */
    private static final DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(30).refresh(Duration.ofMinutes(1));

    /** The instrument tick size. */
    private final Num instrumentTickSize;

    /** The shared websocket connection. */
    private Signal<JsonElement> websocket;

    /**
     * @param marketName
     * @param setting
     */
    FTXService(String marketName, MarketSetting setting) {
        super("BitMEX", marketName, setting);

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
                .flatIterable(JsonElement::getAsJsonArray)
                .map(json -> {
                    return convert(json, increment, previous);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        AtomicLong increment = new AtomicLong();
        Object[] previous = new Object[2];

        return connectWebSocket(Topic.trades).map(json -> {
            System.out.println(json);
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "markets/" + marketName + "/trades?limit=1").flatIterable(e -> e.getAsJsonObject().getAsJsonArray("result"))
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
        return call("GET", "orderBook/L2?depth=1200&symbol=" + marketName).map(e -> convertOrderBook(e.getAsJsonArray()));
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
    private OrderBookPageChanges convertOrderBook(JsonArray array) {
        OrderBookPageChanges change = new OrderBookPageChanges();
        for (JsonElement e : array) {
            JsonObject o = e.getAsJsonObject();
            long id = o.get("id").getAsLong();
            Num price = instrumentTickSize.multiply((100000000L * marketId) - id);
            JsonElement sizeElement = o.get("size");
            double size = sizeElement == null ? 0 : sizeElement.getAsDouble() / price.doubleValue();

            if (o.get("side").getAsString().charAt(0) == 'B') {
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
    private Execution convert(JsonElement json, AtomicLong increment, Object[] previous) {
        JsonObject e = json.getAsJsonObject();
        System.out.println(e);

        Direction direction = Direction.parse(e.get("side").getAsString());
        Num size = Num.of(e.get("size").getAsString());
        Num price = Num.of(e.get("price").getAsString());
        ZonedDateTime date = ZonedDateTime.parse(e.get("time").getAsString(), TimeFormat).withZoneSameLocal(Chrono.UTC);
        long id = e.get("id").getAsLong();

        return Execution.with.direction(direction, size).id(id).price(price).date(date).consecutive(Execution.ConsecutiveDifference);
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JsonElement> call(String method, String path) {
        Request request = new Request.Builder().url("https://ftx.com/api/" + path).build();

        return network.rest(request, Limit).retryWhen(retryPolicy(10, "FTX RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JsonElement> connectWebSocket(Topic topic) {
        WebSocketCommand command = new WebSocketCommand();
        command.op = "subscribe";
        command.channel = topic.name();
        command.market = marketName;

        return network.websocket("wss://ftx.com/ws/", command).flatMap(e -> {
            JsonObject root = e.getAsJsonObject();
            if (root.get("type").getAsString().equals("update")) {
                return I.signal(root.getAsJsonArray("data"));
            } else {
                return I.signal();
            }
        });
    }

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        trades;
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String op;

        public String channel;

        public String market;
    }

    private int estimateConsecutive(Direction direction, ZonedDateTime date, Object[] previous) {
        int consectives = 0;

        if (date.equals(previous[1])) {
            if (direction != previous[0]) {
                consectives = Execution.ConsecutiveDifference;
            } else if (direction == Direction.BUY) {
                consectives = Execution.ConsecutiveSameBuyer;
            } else {
                consectives = Execution.ConsecutiveSameSeller;
            }
        } else {
            consectives = Execution.ConsecutiveDifference;
        }
        previous[0] = direction;
        previous[1] = date;

        return consectives;
    }

    public static void main(String[] args) throws InterruptedException {
        FTX.BTC_USD.executionLatest().to(e -> {
            System.out.println(e);
        });
    }

}
