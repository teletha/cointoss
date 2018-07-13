/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;
import java.util.Objects;

import cointoss.order.Order;
import cointoss.order.OrderBookListChange;
import cointoss.util.Chrono;
import cointoss.util.LogCodec;
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2018/07/12 9:52:55
 */
public abstract class MarketService implements Disposable {

    /** CONSTANTS */
    private static final int ConsecutiveTypeSize = 4;

    /** The exchange name. */
    public final String exchangeName;

    /** The market name. */
    public final String marketName;

    /** The identical market name. */
    public final String fullName;

    /** The execution log. */
    public final MarketLog log;

    /** The network accessor. */
    protected Network network = new Network();

    /**
     * @param exchangeName
     * @param marketName
     */
    protected MarketService(String exchangeName, String marketName) {
        this.exchangeName = Objects.requireNonNull(exchangeName);
        this.marketName = Objects.requireNonNull(marketName);
        this.fullName = exchangeName + " " + marketName;
        this.log = new MarketLog(this);
    }

    /**
     * Estimate the curernt order delay (second).
     * 
     * @return
     */
    public abstract Signal<Integer> delay();

    /**
     * <p>
     * Request order actually.
     * </p>
     * 
     * @param order A order to request.
     * @return A requested order.
     */
    public abstract Signal<String> request(Order order);

    /**
     * <p>
     * Request order canceling.
     * </p>
     * 
     * @param order A order to cancel.
     */
    public abstract Signal<Order> cancel(Order order);

    /**
     * Acquire the execution log between start (exclusive) and end (exclusive) key.
     * 
     * @param key An execution sequencial key (i.e. ID, datetime etc).
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executions(long start, long end);

    /**
     * Acquire the execution log in realtime. This is infinitely.
     * 
     * @return A event stream of execution log.
     */
    public abstract Signal<Execution> executionsRealtimely();

    /**
     * Acquire the my execution log (positions) in realtime. This is infinitely.
     * 
     * @return A event stream of execution log related to my orders.
     */
    public abstract Signal<Execution> executionsRealtimelyForMe();

    /**
     * Acquier the latest execution log.
     * 
     * @return A latest execution log.
     */
    public abstract Execution exectutionLatest();

    /**
     * Acquire the execution sequential key (default is {@link Execution#id}).
     * 
     * @param execution A target execution.
     * @return
     */
    protected long executionKey(Execution execution) {
        return execution.id;
    }

    /**
     * Configure max acquirable execution size per one request.
     * 
     * @return
     */
    protected abstract int executionMaxAcquirableSize();

    /**
     * <p>
     * Request all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> orders();

    /**
     * <p>
     * Get amount of the base currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> baseCurrency();

    /**
     * <p>
     * Get amount of the target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> targetCurrency();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<OrderBookListChange> orderBook();

    /**
     * Build execution from log.
     * 
     * @param values
     * @return
     */
    final Execution decode(Execution previous, String[] values) {
        Execution current = new Execution();
        current.id = decodeId(values[0], previous);
        current.date = decodeDate(values[1], previous);
        current.price = decodePrice(values[2], previous);
        int value = LogCodec.decodeInt(values[3].charAt(0));
        if (value < ConsecutiveTypeSize) {
            current.side = Side.BUY;
            current.consecutive = value;
        } else {
            current.side = Side.SELL;
            current.consecutive = value - ConsecutiveTypeSize;
        }
        current.delay = LogCodec.decodeInt(values[3].charAt(1)) - 3;
        current.size = decodeSize(values[3].substring(2), previous);

        return current;
    }

    /**
     * Build log from execution.
     * 
     * @param execution
     * @return
     */
    final String[] encode(Execution previous, Execution execution) {
        String id = encodeId(execution, previous);
        String time = encodeDate(execution, previous);
        String price = encodePrice(execution, previous);
        String size = encodeSize(execution, previous);
        String delay = LogCodec.encodeInt(execution.delay + 3);
        String sideAndConsecutive = String.valueOf(execution.isBuy() ? execution.consecutive : ConsecutiveTypeSize + execution.consecutive);

        return new String[] {id, time, price, sideAndConsecutive + delay + size};
    }

    /**
     * Decode id.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected long decodeId(String value, Execution previous) {
        return LogCodec.decodeDelta(value, previous.id, 1);
    }

    /**
     * Encode id.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeId(Execution execution, Execution previous) {
        return LogCodec.encodeDelta(execution.id, previous.id, 1);
    }

    /**
     * Decode date.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected ZonedDateTime decodeDate(String value, Execution previous) {
        return LogCodec.decodeDelta(value, previous.date, 0);
    }

    /**
     * Encode date.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeDate(Execution execution, Execution previous) {
        return LogCodec.encodeDelta(execution.date, previous.date, 0);
    }

    /**
     * Decode size.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected Num decodeSize(String value, Execution previous) {
        return LogCodec.decodeDiff(value, previous.size);
    }

    /**
     * Encode size.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeSize(Execution execution, Execution previous) {
        return LogCodec.encodeDiff(execution.size, previous.size);
    }

    /**
     * Decode price.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected Num decodePrice(String value, Execution previous) {
        return LogCodec.decodeDiff(value, previous.price);
    }

    /**
     * Encode price.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodePrice(Execution execution, Execution previous) {
        return LogCodec.encodeDiff(execution.price, previous.price);
    }

    /**
     * Calculate human-readable price for display.
     * 
     * @param price A target price.
     * @return
     */
    public String calculateReadablePrice(double price) {
        return Num.of(price).scale(0).toString();
    }

    /**
     * Calculate human-readable price for display.
     * 
     * @param seconds A target time. (second)
     * @return
     */
    public String calculateReadableTime(double seconds) {
        ZonedDateTime time = Chrono.systemBySeconds(seconds);

        if (time.getMinute() == 0 && time.getHour() % 6 == 0) {
            return time.format(Chrono.DateTimeWithoutSec);
        } else {
            return time.format(Chrono.TimeWithoutSec);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return fullName;
    }
}
