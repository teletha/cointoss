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

import kiss.Signal;

/**
 * @version 2017/07/24 23:39:15
 */
public interface MarketBackend {

    /**
     * <p>
     * Initialize and activate market backend.
     * </p>
     * 
     * @param market
     */
    void initialize(Market market, MarketLogBuilder builder);

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
    Signal<Order> getOrdersBy(OrderState state);

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
     * Get amount of the base and target currency.
     * </p>
     * 
     * @return
     */
    Signal<BalanceUnit> getCurrency();
}
