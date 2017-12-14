/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import cointoss.Order.State;
import cointoss.order.OrderBookChange;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2017/09/08 18:51:05
 */
public interface MarketBackend extends Disposable {

    /**
     * <p>
     * Initialize and activate market backend.
     * </p>
     * 
     * @param market
     */
    void initialize(Market market, Signal<Execution> log);

    /**
     * <p>
     * Request order actually.
     * </p>
     * 
     * @param order
     * @return
     */
    Signal<String> request(Order order);

    /**
     * <p>
     * Request order canceling by id.
     * </p>
     * 
     * @param childOrderId
     */
    Signal<String> cancel(String childOrderId);

    /**
     * <p>
     * Request order canceling by order.
     * </p>
     * 
     * @param order
     */
    default Signal<Order> cancel(Order order) {
        return cancel(order.child_order_acceptance_id).mapTo(order).effect(o -> {
            o.state.set(State.CANCELED);
        });
    }

    /**
     * <p>
     * Request order by id.
     * </p>
     * 
     * @return
     */
    Signal<Order> getOrderBy(String id);

    /**
     * <p>
     * Check all orders.
     * </p>
     * 
     * @return
     */
    Signal<Order> getOrders();

    /**
     * <p>
     * Check all orders.
     * </p>
     * 
     * @return
     */
    Signal<Order> getOrdersBy(State state);

    /**
     * <p>
     * Check all positions.
     * </p>
     * 
     * @return
     */
    Signal<Position> getPositions();

    /**
     * <p>
     * Check all executions.
     * </p>
     * 
     * @return
     */
    Signal<Execution> getExecutions();

    /**
     * <p>
     * Get amount of the base currency.
     * </p>
     * 
     * @return
     */
    Signal<Num> getBaseCurrency();

    /**
     * <p>
     * Get amount of the target currency.
     * </p>
     * 
     * @return
     */
    Signal<Num> getTargetCurrency();

    /**
     * <p>
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    Signal<OrderBookChange> getOrderBook();

    /**
     * Get the service status.
     * 
     * @return
     */
    Signal<Health> getHealth();

    /**
     * @version 2017/12/14 16:20:52
     */
    enum Health {
        Normal, Busy, VeryBusy, SuperBusy, NoOrder, Stop;
    }
}
