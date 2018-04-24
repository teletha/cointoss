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

import cointoss.order.Order;
import cointoss.order.OrderBookListChange;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2018/04/25 2:37:12
 */
public abstract class MarketBackend implements Disposable {

    /**
     * <p>
     * Initialize and activate market backend.
     * </p>
     * 
     * @param market
     */
    protected abstract void initialize(Market market, Signal<Execution> log);

    /**
     * Get the server health (status).
     * 
     * @return
     */
    public abstract Signal<MarketHealth> health();

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
     * Acquire the execution log after the specified ID as much as possible.
     * 
     * @param id
     * @return
     */
    public abstract Signal<Execution> executions(long id);

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
     * Check all positions.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Position> getPositions();

    /**
     * <p>
     * Get amount of the base currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> getBaseCurrency();

    /**
     * <p>
     * Get amount of the target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<Num> getTargetCurrency();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    public abstract Signal<OrderBookListChange> getOrderBook();

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
