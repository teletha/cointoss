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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.execution.ExecutionLog;
import cointoss.order.Order;
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

    /** The shared websocket connection. */
    private Signal<JsonElement> websocket;

    /**
     * @param marketName
     * @param setting
     */
    BinanceService(String marketName, MarketSetting setting) {
        super("Binance", marketName, setting);
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
        return call("GET", "aggTrades?symbol=" + marketName.toUpperCase() + "&limit=" + (end - start) + "&fromId=" + start)
                .flatIterable(JsonElement::getAsJsonArray)
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
        return call("GET", "aggTrades?symbol=" + marketName.toUpperCase() + "&limit=1").flatIterable(JsonElement::getAsJsonArray)
                .map(e -> convert(e.getAsJsonObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateInitialExecutionId() {
        return 224601247;
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
     * Estimate consecutive type.
     * 
     * @param previous
     */
    private int estimateConsecutiveType(String prevBuyer, String prevSeller, String buyer, String seller) {
        if (buyer.equals(prevBuyer)) {
            if (seller.equals(prevSeller)) {
                return Execution.ConsecutiveSameBoth;
            } else {
                return Execution.ConsecutiveSameBuyer;
            }
        } else if (seller.equals(prevSeller)) {
            return Execution.ConsecutiveSameSeller;
        } else {
            return Execution.ConsecutiveDifference;
        }
    }

    /**
     * Analyze Taker's order ID and obtain approximate order time (Since there is a bot which
     * specifies non-standard id format, ignore it in that case).
     */
    private int estimateDelay(long eventTime, long tradeTime) {
        int delay = (int) (eventTime - tradeTime) / 1000;

        if (delay < 0) {
            return Execution.DelayInestimable;
        } else if (180 < delay) {
            return Execution.DelayHuge;
        } else {
            return delay;
        }
    }

    /**
     * Call rest API.
     * 
     * @param method
     * @param path
     * @return
     */
    private Signal<JsonElement> call(String method, String path) {
        Request request = new Request.Builder().url("https://api.binance.com/api/v3/" + path).build();

        return network.rest(request, Limit).retryWhen(retryPolicy(10, "BitMEX RESTCall"));
    }

    /**
     * Build shared websocket connection for this market.
     * 
     * @return
     */
    private synchronized Signal<JsonObject> connectSharedWebSocket(Topic topic) {
        String name = marketName + "@" + topic;

        if (websocket == null) {
            WebSocketCommand command = new WebSocketCommand();
            command.method = "SUBSCRIBE";
            for (Topic type : Topic.values()) {
                command.params.add(marketName + "@" + type);
            }
            websocket = network.websocket("wss://stream.binance.com:9443/stream", command);
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
        aggTrade;
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

    public static void main1(String[] args) {
        Binance.BTC_USDT.executionsRealtimely().to(e -> {
        }, e -> {
            e.printStackTrace();
        });
    }

    public static void main(String[] args) {
        new ExecutionLog(Binance.BTC_USDT).fromYestaday().to(e -> {
        });
    }
}
