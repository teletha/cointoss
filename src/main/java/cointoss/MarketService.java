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
import cointoss.util.Network;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2018/04/29 17:28:36
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
    protected abstract Execution decode(String[] values, Execution previous);

    /**
     * Build log from execution.
     * 
     * @param execution
     * @return
     */
    protected abstract String[] encode(Execution execution, Execution previous);
}
