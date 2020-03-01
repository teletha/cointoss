/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

class BinanceService extends MarketService {

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(600).refresh(Duration.ofMinutes(1));

    /** The market type. */
    private final boolean isFutures;

    /** The shared websocket connection. */
    private Signal<JsonElement> websocket;

    /**
     * @param marketName
     * @param setting
     */
    BinanceService(String marketName, boolean isFutures, MarketSetting setting) {
        super(isFutures ? "BinanceF" : "Binance", marketName, setting);

        this.isFutures = isFutures;
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
    public Signal<Execution> executions(long start, long end) {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1000&fromId=" + (start + 1)).flatIterable(JsonElement::getAsJsonArray)
                .map(e -> convert(e.getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<Execution> connectExecutionRealtimely() {
        return connectSharedWebSocket(Topic.aggTrade).map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return call("GET", "aggTrades?symbol=" + marketName + "&limit=1").flatIterable(JsonElement::getAsJsonArray)
                .map(e -> convert(e.getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return 0;
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
        return call("GET", "depth?symbol=" + marketName + "&limit=1000").map(e -> convertOrderBook(e, "bids", "asks"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Signal<OrderBookPageChanges> connectOrderBookRealtimely() {
        String bidName = isFutures ? "b" : "bids";
        String askName = isFutures ? "a" : "asks";

        return connectSharedWebSocket(Topic.depth20$100ms).map(e -> convertOrderBook(e, bidName, askName));
    }

    /**
     * Convert json to {@link OrderBookPageChanges}.
     * 
     * @param array
     * @return
     */
    private OrderBookPageChanges convertOrderBook(JsonElement root, String bidName, String askName) {
        OrderBookPageChanges change = new OrderBookPageChanges();

        JsonObject o = root.getAsJsonObject();
        JsonArray bids = o.get(bidName).getAsJsonArray();
        JsonArray asks = o.get(askName).getAsJsonArray();

        for (JsonElement e : bids) {
            JsonArray bid = e.getAsJsonArray();
            Num price = Num.of(bid.get(0).getAsBigDecimal());
            double size = bid.get(1).getAsDouble();

            change.bids.add(new OrderBookPage(price, size));
        }

        for (JsonElement e : asks) {
            JsonArray ask = e.getAsJsonArray();
            Num price = Num.of(ask.get(0).getAsBigDecimal());
            double size = ask.get(1).getAsDouble();

            change.asks.add(new OrderBookPage(price, size));
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> baseCurrency() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Num> targetCurrency() {
        return I.signal();
    }

    /**
     * Convert to {@link Execution}.
     * 
     * @param json
     * @param previous
     * @return
     */
    private Execution convert(JsonObject e) {
        Direction direction = e.get("m").getAsBoolean() ? Direction.SELL : Direction.BUY;
        Num size = Num.of(e.get("q").getAsString());
        Num price = Num.of(e.get("p").getAsString());
        long tradeTime = e.get("T").getAsLong();
        ZonedDateTime date = Chrono.utcByMills(tradeTime);
        long tradeId = e.get("a").getAsLong();

        Execution exe = Execution.with.direction(direction, size)
                .id(tradeId)
                .price(price)
                .date(date)
                .consecutive(Execution.ConsecutiveDifference)
                .delay(Execution.DelayInestimable);

        return exe;
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JsonElement> call(String method, String path) {
        String uri = isFutures ? "https://fapi.binance.com/fapi/v1/" : "https://api.binance.com/api/v3/";
        Request request = new Request.Builder().url(uri + path).build();

        return network.rest(request, Limit).retryWhen(retryPolicy(10, "Binance RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JsonObject> connectSharedWebSocket(Topic topic) {
        String uri = isFutures ? "wss://fstream.binance.com/stream" : "wss://stream.binance.com:9443/stream";
        String name = marketName.toLowerCase() + "@" + topic;

        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.method = "SUBSCRIBE";
            for (Topic type : Topic.values()) {
                command.params.add(marketName.toLowerCase() + "@" + type);
            }
            websocket = network.websocket(uri, command);
        }

        return websocket.share().flatMap(e -> {
            JsonObject root = e.getAsJsonObject();
            JsonElement stream = root.get("stream");
            if (stream == null || !stream.getAsString().equals(name)) {
                return I.signal();
            } else {
                return I.signal(root.get("data")).map(JsonElement::getAsJsonObject);
            }
        });
    }

    /**
     * Subscription topics for websocket.
     */
    private enum Topic {
        aggTrade, depth20$100ms;

        @Override
        public String toString() {
            return name().replace('$', '@');
        }
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String method;

        public List<String> params = new ArrayList();

        public int id;
    }
}
