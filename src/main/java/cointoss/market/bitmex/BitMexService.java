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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderBookChange;
import cointoss.order.OrderState;
import cointoss.util.APILimiter;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Ⅲ;
import okhttp3.Request;

class BitMexService extends MarketService {

    /** The realtime data format */
    private static final DateTimeFormatter RealTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /** The bitflyer API limit. */
    private static final APILimiter Limit = APILimiter.with.limit(60).refresh(Duration.ofMinutes(1));

    /**
     * @param marketName
     * @param setting
     */
    BitMexService(String marketName, MarketSetting setting) {
        super("BitMEX", marketName, setting);
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
    public Signal<String> request(Order order, Consumer<OrderState> state) {
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
        AtomicInteger increment = new AtomicInteger();
        Object[] consectives = new Object[2];

        return call("GET", "trade?symbol=" + marketName + "&count=" + (end - start) + "&start=" + start)
                .flatIterable(JsonElement::getAsJsonArray)
                .map(json -> {
                    return convert(json, start, increment, consectives);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionsRealtimely() {
        WebSocketCommand command = new WebSocketCommand();
        command.op = "subscribe";
        command.args.add("trade:" + marketName);

        long start = 0;
        AtomicInteger increment = new AtomicInteger();
        Object[] consecutives = new Object[2];

        return network.websocket("wss://www.bitmex.com/realtime", command).flatMap(json -> {
            JsonArray array = json.getAsJsonObject().getAsJsonArray("data");

            if (array == null) {
                return I.signal();
            } else {
                return I.signal(array).map(e -> convert(e, start, increment, consecutives));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Ⅲ<Direction, String, Execution>> executionsRealtimelyForMe() {
        return I.signal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> executionLatest() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSequencialId() {
        return false;
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
    public Signal<OrderBookChange> orderBook() {
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
    private Execution convert(JsonElement json, long start, AtomicInteger increment, Object[] consectives) {
        JsonObject e = json.getAsJsonObject();

        Direction direction = Direction.parse(e.get("side").getAsString());
        Num size = Num.of(e.get("homeNotional").getAsString());
        Num price = Num.of(e.get("price").getAsString());
        ZonedDateTime date = ZonedDateTime.parse(e.get("timestamp").getAsString(), RealTimeFormat);

        int consecutive = Execution.ConsecutiveDifference;

        if (consectives[0] == direction && consectives[1].equals(date)) {
            consecutive = direction == Direction.BUY ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;
        }
        consectives[0] = direction;
        consectives[1] = date;

        return Execution.with.direction(direction, size)
                .id(start + increment.getAndIncrement())
                .price(price)
                .date(date)
                .consecutive(consecutive)
                .info(e.get("trdMatchID").getAsString());
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

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class WebSocketCommand {

        public String op;

        public List<String> args = new ArrayList();
    }
}
