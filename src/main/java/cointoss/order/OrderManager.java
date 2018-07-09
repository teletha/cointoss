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
 * @version 2018/07/09 2:04:58
 */
public final class OrderManager {

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
        added.to(managed::add);
        removed.to(managed::remove);

        service.add(service.positions().to(exe -> {
            for (Order order : managed) {
                if (order.id.is(exe.yourOrder)) {
                    Num executed = exe.size;

                    if (order.type.isMarket() && executed.isNot(0)) {
                        order.price.set(v -> v.multiply(order.sizeExecuted)
                                .plus(exe.price.multiply(executed))
                                .divide(executed.plus(order.sizeExecuted)));
                    }

                    order.sizeExecuted = order.sizeExecuted.plus(executed);
                    order.sizeRemaining = order.sizeRemaining.minus(executed);

                    if (order.sizeRemaining.is(Num.ZERO)) {
                        order.state.set(OrderState.COMPLETED);
                    }

                    // pairing order and execution
                    order.attribute(RecordedExecutions.class).record(exe);

                    updates.accept(I.pair(order, exe));
                    return;
                }
            }
        }));
    }

    /**
     * Build the {@link Signal} which requests the specified {@link Order} to the market. This method
     * DON'T request order, you MUST subscribe {@link Signal}. If you want to request actually, you can
     * use {@link #requestNow(Order)}.
     * 
     * @param order A order to request.
     * @return A order request process.
     * @see #requestNow(Order)
     */
    public Signal<Order> request(Order order) {
        order.state.set(REQUESTING);

        return service.request(order).retryWhen(fail -> fail.effect(e -> {
            System.out.println("Fail " + order + "  retry ");
            e.printStackTrace();
        }).take(20).delay(500, MILLISECONDS)).map(id -> {
            order.id.let(id);
            order.created.set(ZonedDateTime.now());
            order.state.set(ACTIVE);
            order.observeTerminating().to(remove::accept);

            addition.accept(order);

            return order;
        }).effectOnError(e -> {
            order.state.set(OrderState.CANCELED);
        });
    }

    /**
     * Request the specified {@link Order} to the market actually.
     * 
     * @param order A order to request.
     * @return A requested {@link Order}.
     * @see #request(Order)
     */
    public Order requestNow(Order order) {
        request(order).to(I.NoOP);

        return order;
    }

    /**
     * Build the {@link Signal} which cancels the specified {@link Order} from the market. This method
     * DON'T cancel order, you MUST subscribe {@link Signal}. If you want to cancel actually, you can
     * use {@link #cancelNow(Order)}.
     * 
     * @param order A order to cancel.
     * @return A order cancel process.
     * @see #cancelNow(Order)
     */
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
     * Cancel the specified {@link Order} from the market actually.
     * 
     * @param order A order to request.
     * @return A canceled {@link Order}.
     * @see #cancel(Order)
     */
    public Order cancelNow(Order order) {
        cancel(order).to(I.NoOP);

        return order;
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

}
