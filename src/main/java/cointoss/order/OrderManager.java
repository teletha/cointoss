/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import static cointoss.order.OrderState.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;
import kiss.Ⅱ;

/**
 * @version 2018/07/17 0:08:29
 */
public final class OrderManager {

    /** The actual service. */
    private final MarketService service;

    /** The actual order manager. */
    private final List<Order> managed = new CopyOnWriteArrayList();

    /** The unmodifiable exposed active orders. */
    public final List<Order> items = Collections.unmodifiableList(managed);

    /** The order adding event. */
    private final Signaling<Order> addition = new Signaling();

    /** The order adding event. */
    public final Signal<Order> add = addition.expose;

    /** The order removed event. */
    private final Signaling<Order> remove = new Signaling();

    /** The order removed event. */
    public final Signal<Order> removed = remove.expose;

    /** The order update event. */
    private final Signaling<Ⅱ<Order, Execution>> updates = new Signaling();

    /** The order update event. */
    public final Signal<Ⅱ<Order, Execution>> updated = updates.expose;

    /** The current position direction. */
    public final Variable<Direction> positionDirection = Variable.empty();

    /** The current total position size. */
    public final Variable<Num> positionSize = Variable.of(Num.ZERO);

    /** The curretn position average price. */
    public final Variable<Num> positionPrice = Variable.of(Num.ZERO);

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;
        add.to(managed::add);
        removed.to(managed::remove);

        // retrieve orders on server
        // don't use orders().to(addition); it completes addition signaling itself
        service.orders().to(addition::accept);

        // retrieve orders on realtime
        service.add(service.executionsRealtimelyForMe().to(v -> {
            // manage position
            String id = v.ⅱ;
            Execution execution = v.ⅲ;

            // manage order
            for (Order order : managed) {
                if (order.id.equals(id)) {
                    Num executed = execution.size;

                    if (order.type.isTaker() && executed.isNot(0)) {
                        order.assignPrice(n -> n.multiply(order.executedSize)
                                .plus(execution.price.multiply(executed))
                                .divide(executed.plus(order.executedSize)));
                    }
                    order.executed(execution);

                    if (order.remainingSize.is(Num.ZERO)) {
                        order.setState(COMPLETED);
                    }

                    updates.accept(I.pair(order, execution));
                    return;
                }
            }
        }));
    }

    /**
     * Stream for the current managed {@link Order}s and the incoming {@link Order}s.
     * 
     * @return
     */
    public Signal<Order> manages() {
        return I.signal(managed).merge(add);
    }

    /**
     * Build the {@link Signal} which requests the specified {@link Order} to the market. This
     * method DON'T request order, you MUST subscribe {@link Signal}. If you want to request
     * actually, you can use {@link #requestNow(Order)}.
     * 
     * @param order A order to request.
     * @return A order request process.
     * @see #requestNow(Order)
     */
    public Signal<Order> request(Order order) {
        if (order.state == OrderState.INIT || order.state == OrderState.REQUESTING) {
            order.setState(REQUESTING);
            addition.accept(order);

            return service.request(order, order::setState).retryWhen(service.setting.retryPolicy()).map(id -> {
                order.setState(ACTIVE);
                order.assignId(id);
                order.assignCreationTime(service.now());
                order.observeTerminating().to(remove::accept);

                return order;
            }).effectOnError(e -> {
                order.setState(CANCELED);
            });
        } else {
            return I.signal(order);
        }
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
     * Build the {@link Signal} which cancels the specified {@link Order} from the market. This
     * method DON'T cancel order, you MUST subscribe {@link Signal}. If you want to cancel actually,
     * you can use {@link #cancelNow(Order)}.
     * 
     * @param order A order to cancel.
     * @return A order cancel process.
     * @see #cancelNow(Order)
     */
    public Signal<Order> cancel(Order order) {
        if (order.state == ACTIVE || order.state == REQUESTING) {
            OrderState previous = order.state;
            order.setState(REQUESTING);

            return service.cancel(order).retryWhen(service.setting.retryPolicy()).effect(result -> {
                System.out.println("Canceled " + result.ⅰ + " " + result.ⅱ);
                managed.remove(order);
                order.setExecutedSize(result.ⅱ);
                order.setRemainingSize(order.size.minus(result.ⅱ));
                order.setState(result.ⅰ);
            }).effectOnError(e -> {
                order.setState(previous);
            }).mapTo(order);
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
     * Cancel all orders.
     */
    public void cancelNowAll() {
        for (Order order : items) {
            cancelNow(order);
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
}
