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

import static cointoss.order.OrderState.*;
import static java.util.concurrent.TimeUnit.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.Execution;
import cointoss.MarketService;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Ⅱ;

/**
 * @version 2018/04/28 21:27:16
 */
public class OrderManager {

    /** The actual service. */
    private final MarketService service;

    /** The actual order manager. */
    private final List<Order> managed = new CopyOnWriteArrayList();

    /** The unmodifiable exposed active orders. */
    public final List<Order> items = Collections.unmodifiableList(managed);

    /** The order remove event. */
    private final Signaling<Order> remove = new Signaling();

    /** The order remove event. */
    public final Signal<Order> removed = remove.expose;

    /** The order add event. */
    private final Signaling<Order> addition = new Signaling();

    /** The order add event. */
    public final Signal<Order> added = addition.expose;

    /** The order update event. */
    private final Signaling<Ⅱ<Order, Execution>> updates = new Signaling();

    /** The order update event. */
    public final Signal<Ⅱ<Order, Execution>> updated = updates.expose;

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;

        service.add(service.positions().to(e -> {
            for (Order order : managed) {
                if (order.id.is(e.yourOrder)) {
                    update(order, e);
                }
            }
        }));
    }

    public Signal<Order> request(Order order) {
        order.state.set(REQUESTING);

        return service.request(order).retryWhen(fail -> fail.effect(e -> {
            System.out.println("Fail " + order + "  retry ");
            e.printStackTrace();
        }).take(40).delay(100, MILLISECONDS)).map(id -> {
            order.id.let(id);
            order.created.set(ZonedDateTime.now());
            order.sizeRemaining.set(order.size);
            order.state.set(ACTIVE);
            managed.add(order);

            return order;
        }).effectOnError(e -> {
            order.state.set(OrderState.CANCELED);
        });
    }

    public Signal<Order> cancel(Order order) {
        if (order.state.is(ACTIVE) || order.state.is(OrderState.REQUESTING)) {
            OrderState previous = order.state.set(REQUESTING);

            return service.cancel(order).effect(o -> {
                managed.remove(o);
                o.state.set(CANCELED);
            }).effectOnError(e -> {
                order.state.set(previous);
            });
        } else {
            return I.signal(order);
        }
    }

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasActiveOrder() {
        return managed.isEmpty() == false;
    }

    /**
     * Check the order state.
     * 
     * @return A result.
     */
    public boolean hasNoActiveOrder() {
        return managed.isEmpty() == true;
    }

    /**
     * Add new order.
     * 
     * @param order
     */
    public void add(Order order) {
        if (order != null && !managed.contains(order)) {
            managed.add(order);
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
            order.state.set(OrderState.COMPLETED);
            managed.remove(order); // complete order
        }

        // pairing order and execution
        order.attribute(RecordedExecutions.class).record(exe);

        updates.accept(I.pair(order, exe));
    }
}
