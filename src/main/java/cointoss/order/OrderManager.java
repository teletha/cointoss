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
     * Update local managed {@link Order}.
     * 
     * @param order
     * @param exe
     */
    private void update(Order order, Execution exe) {
        // for order state
        Num executed = Num.min(order.remainingSize, exe.size);

        if (order.child_order_type.isMarket() && executed.isNot(0)) {
            order.averagePrice.set(v -> v.multiply(order.executed_size)
                    .plus(exe.price.multiply(executed))
                    .divide(executed.plus(order.executed_size)));
        }

        order.executed_size.set(v -> v.plus(executed));
        order.remainingSize.set(v -> v.minus(executed));

        if (order.remainingSize.is(Num.ZERO)) {
            order.state.set(State.COMPLETED);
            orders.remove(order); // complete order
        }

        // pairing order and execution
        order.executions.add(exe);
    }
}
