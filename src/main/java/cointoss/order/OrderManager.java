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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.MarketService;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * 
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

    /** The managed position size and direction. */
    public final Variable<Num> position = Variable.of(Num.ZERO);

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;
        add.to(managed::add);
        removed.to(managed::remove);

        // retrieve orders on server
        // don't use orders().to(addition); it completes addition signaling itself
        service.orders(OrderState.ACTIVE).retryWhen(service.retryPolicy(5)).to(addition::accept);

        // retrieve orders on realtime
        service.add(service.ordersRealtimely().to(updater -> {
            for (Order order : managed) {
                if (order.id.equals(updater.id)) {
                    update(order, updater);
                    return;
                }
            }
        }));
    }

    /**
     * Update order state.
     * 
     * @param order Your order to update.
     * @param updater A new order info.
     */
    private void update(Order order, Order updater) {
        if (updater.isBuy()) {
            position.set(v -> v.minus(order.executedSize).plus(updater.executedSize));
        } else {
            position.set(v -> v.plus(order.executedSize).minus(updater.executedSize));
        }

        order.setPrice(updater.price);
        order.updateAtomically(updater.remainingSize, updater.executedSize);
        order.setState(updater.state);

        if (updater.isTerminated()) {
            order.setTerminationTime(service.now());
        }
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

            Complementer complementer = new Complementer(order);

            return service.request(order)
                    .retryWhen(service.retryPolicy(5))
                    .effectOnObserve(complementer::start)
                    .effect(complementer::complement)
                    .effectOnTerminate(complementer::stop)
                    .map(id -> {
                        order.setState(ACTIVE);
                        order.setId(id);
                        order.setCreationTime(service.now());

                        return order;
                    })
                    .effectOnError(e -> {
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

            return service.cancel(order).retryWhen(service.retryPolicy(5)).effectOnError(e -> {
                order.setState(previous);
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

    /**
     * <p>
     * Comlement executions while order request and response.
     * </p>
     * <p>
     * If the execution data comes to the real-time API before the oreder's response, the order
     * cannot be identified from the real-time API.
     * </p>
     * <p>
     * Record all execution data from request to response, and check if there is already an
     * execution data at response.
     * </p>
     */
    private class Complementer {

        /** The associated order. */
        private final Order order;

        /** The realtime order manager. */
        private final LinkedList<Order> orders = new LinkedList();

        /** The disposer for realtime execution stream. */
        private Disposable disposer;

        /**
         * 
         */
        private Complementer(Order order) {
            this.order = order;
        }

        /**
         * Start complementing.
         */
        private void start() {
            disposer = service.ordersRealtimely().to(orders::add);
        }

        /**
         * Stop complementing.
         */
        private void stop() {
            disposer.dispose();
        }

        /**
         * Because ID registration has been completed, it is possible to detect contracts from the
         * real-time API. Check if there is an order in the execution data recorded after placing
         * the order.
         */
        private void complement(String orderId) {
            // stop recording realtime executions and register order id atomically
            disposer.dispose();
            addition.accept(order);

            // check order executions while request and response
            orders.forEach(e -> {
                if (e.id.equals(orderId)) {
                    update(order, e);
                }
            });

            // order termination will unregister
            order.observeTerminating().to(remove::accept);
        }
    }
}
