/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.Execution;
import cointoss.order.Order.State;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Signaling;

/**
 * @version 2018/04/28 21:27:16
 */
public class OrderManager {

    /** The actual order manager. */
    private final List<Order> orders = new CopyOnWriteArrayList();

    /** The unmodifiable active orders. */
    public final List<Order> items = Collections.unmodifiableList(orders);

    /** The order remove event. */
    private final Signaling<Order> remove = new Signaling();

    /** The order remove event. */
    public final Signal<Order> removed = remove.expose;

    /** The order add event. */
    private final Signaling<Order> addition = new Signaling();

    /** The order add event. */
    public final Signal<Order> added = addition.expose;

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasActiveOrder() {
        return orders.isEmpty() == false;
    }

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasNoActiveOrder() {
        return orders.isEmpty() == true;
    }

    /**
     * Add new order.
     * 
     * @param order
     */
    public void add(Order order) {
        if (order != null && !orders.contains(order)) {
            orders.add(order);
            addition.accept(order);
        }
    }

    /**
     * Update local managed {@link Order}.
     * 
     * @param order
     * @param exe
     */
    private void update(Order order, Execution exe) {
        // for order state
        Num executed = Num.min(order.sizeRemaining, exe.size);

        if (order.type.isMarket() && executed.isNot(0)) {
            order.price
                    .set(v -> v.multiply(order.sizeExecuted).plus(exe.price.multiply(executed)).divide(executed.plus(order.sizeExecuted)));
        }

        order.sizeExecuted.set(v -> v.plus(executed));
        order.sizeRemaining.set(v -> v.minus(executed));

        if (order.sizeRemaining.is(Num.ZERO)) {
            order.state.set(State.COMPLETED);
            orders.remove(order); // complete order
        }

        // pairing order and execution
        order.executions.add(exe);
    }
}
