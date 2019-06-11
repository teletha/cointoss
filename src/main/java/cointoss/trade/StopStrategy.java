/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import cointoss.order.Order;
import cointoss.order.OrderManager;
import kiss.I;

public interface StopStrategy extends BiConsumer<OrderManager, Order> {

    /** The commonly used strategy. */
    StopStrategy Take = (manager, order) -> {
        manager.requestNow(order);
    };

    /** The commonly used strategy. */
    StopStrategy Make = (manager, order) -> {
        manager.requestNow(order);
    };

    /**
     * We will place an order with limit price first. If all the orders have not been executed after
     * the specified time has passed, the limit will be canceled and the taker order will be placed.
     * 
     * @param time A time value.
     * @param unit A time unit.
     * @return A create {@link StopStrategy}.
     */
    static StopStrategy makeThenTakeAfter(long time, ChronoUnit unit) {
        return (manager, order) -> {
            manager.request(order).to(() -> {
                I.schedule(time, TimeUnit.of(unit), () -> {
                    if (order.isNotCompleted()) {
                        manager.cancel(order).to(() -> {
                            if (order.isNotCompleted()) {
                                manager.request(Order.with.direction(order.direction, order.remainingSize)).to(taker -> {

                                });
                            }
                        });
                    }
                });
            });
        };
    }

    /**
     * We will place an order with limit price first. If the order has not been executed even after
     * the specified time has passed, the limit price will be canceled.
     * 
     * @param time A time value.
     * @param unit A time unit.
     * @return A create {@link StopStrategy}.
     */
    static StopStrategy makeThenCancelAfter(long time, ChronoUnit unit) {
        return (manager, order) -> {
            manager.request(order).to(() -> {
                I.schedule(time, TimeUnit.of(unit), () -> {
                    if (order.isNotCompleted()) {
                        manager.cancelNow(order);
                    }
                });
            });
        };
    }
}
