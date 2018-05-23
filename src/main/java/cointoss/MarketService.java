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
import cointoss.util.LogCodec;
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2018/05/23 17:25:31
 */
public abstract class MarketService implements Disposable {

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
     * Acquire the market starting date.
     * 
     * @return
     */
    public abstract ZonedDateTime start();

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
     * Acquire the execution log in realtime.
     * 
     * @return
     */
    public abstract Signal<Execution> executions();

    /**
     * Acquire the execution log after the specified key (maybe ID) as much as possible.
     * 
     * @param key An execution sequencial key (i.e. ID, datetime etc).
     * @return This {@link Signal} will be completed immediately.
     */
    public abstract Signal<Execution> executions(long key);

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
     * <p>
     * Request all orders.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Order> orders();

    /**
     * Acquire the your position log in realtime.
     * 
     * @return
     */
    public abstract Signal<Execution> positions();

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
    final Execution decode(String[] values, Execution previous) {
        if (previous == null) {
            return new Execution(values);
        } else {
            Execution current = new Execution();
            current.id = decodeId(values[0], previous);
            current.exec_date = decodeDate(values[1], previous);
            current.price = decodePrice(values[2], previous);
            int value = LogCodec.decodeInt(values[3].charAt(0));
            if (value < 3) {
                current.side = Side.BUY;
                current.consecutive = value;
            } else {
                current.side = Side.SELL;
                current.consecutive = value - 3;
            }
            current.size = decodeSize(values[3].substring(1), previous);

            return current;
        }
    }

    /**
     * Build log from execution.
     * 
     * @param execution
     * @return
     */
    final String[] encode(Execution execution, Execution previous) {
        if (previous == null) {
            // no diff
            return execution.toString().split(" ");
        } else {
            String id = encodeId(execution, previous);
            String time = encodeDate(execution, previous);
            String price = encodePrice(execution, previous);
            String size = encodeSize(execution, previous);
            String sideAndConsecutive = String.valueOf(execution.side.isBuy() ? execution.consecutive : 3 + execution.consecutive);

            return new String[] {id, time, price, sideAndConsecutive + size};
        }
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
        return LogCodec.decodeDelta(value, previous.exec_date, 0);
    }

    /**
     * Encode date.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeDate(Execution execution, Execution previous) {
        return LogCodec.encodeDelta(execution.exec_date, previous.exec_date, 0);
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
}
