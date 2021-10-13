/*
 * Copyright (C) 2021 cointoss Development Team
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

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.util.arithmetic.Num;
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
    private final Signaling<Order> add = new Signaling();

    /** The order adding event. */
    public final Signal<Order> added = add.expose;

    /** The order removed event. */
    private final Signaling<Order> remove = new Signaling();

    /** The order removed event. */
    public final Signal<Order> removed = remove.expose;

    /** The compound size and direction (minus means short position). */
    public final Variable<Num> compoundSize = Variable.of(Num.ZERO);

    /** The compound average position price. */
    public final Variable<Num> compoundPrice = Variable.of(Num.ZERO);

    /** The compound total position price. */
    private Num compoundTotalPrice = Num.ZERO;

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;
        added.to(managed::add);
        removed.to(managed::remove);

        // retrieve orders on server
        // don't use orders().to(addition); it completes addition signaling itself
        service.orders(OrderState.ACTIVE).retry(service.retryPolicy(5)).to(this::update);

        // retrieve orders on realtime
        service.add(service.ordersRealtimely().to(this::update));
    }

    /**
     * Update order.
     * 
     * @param updater
     */
    final void update(Order updater) {
        for (Order order : managed) {
            if (order.id.equals(updater.id)) {
                switch (updater.state) {
                case CANCELED:
                    cancel(order, updater);
                    return;

                case ACTIVE:
                    updateFully(order, updater);
                    return;

                case ACTIVE_PARTIAL:
                    updatePartially(order, updater);
                    return;

                default:
                    return;
                }
            }
        }
        add(updater);
    }

    /**
     * Add new manageable order.
     * 
     * @param order
     */
    private void add(Order order) {
        // calculate position
        calculateCompoundPosition(order.direction, Num.ZERO, Num.ZERO, order.price, order.executedSize);

        // store order and fire add event
        add.accept(order);
    }

    /**
     * Cancel order.
     * 
     * @param order
     * @param updater
     */
    private void cancel(Order order, Order updater) {
        order.setState(OrderState.CANCELED);
        order.setTerminationTime(service.now());
    }

    /**
     * Update order state.
     * 
     * @param order Your order to update.
     * @param updater A new order info.
     */
    private void updateFully(Order order, Order updater) {
        // calculate position
        calculateCompoundPosition(order.direction, order.price, order.executedSize, updater.price, updater.size);

        order.setPrice(updater.price);
        order.setExecutedSize(updater.size);
        order.setCommission(updater.commission);

        if (order.size.is(order.executedSize)) {
            order.setState(OrderState.COMPLETED);
            order.setTerminationTime(service.now());
        }
    }

    /**
     * Update order state.
     * 
     * @param order Your order to update.
     * @param updater A new order info.
     */
    private void updatePartially(Order order, Order updater) {
        // calculate position
        calculateCompoundPosition(order.direction, order.price, order.executedSize, updater.price, updater.executedSize);

        Num newExecutedSize = order.executedSize.plus(updater.size);
        Num newAveragePrice = order.price.multiply(order.executedSize)
                .plus(updater.price.multiply(updater.size))
                .divide(newExecutedSize)
                .scale(service.setting.base.scale);
        Num newCommission = order.commission.plus(updater.commission);

        order.setPrice(newAveragePrice);
        order.setExecutedSize(newExecutedSize);
        order.setCommission(newCommission);

        if (order.size.is(order.executedSize)) {
            order.setState(OrderState.COMPLETED);
            order.setTerminationTime(service.now());
        }
    }

    /**
     * Update the compound position.
     * 
     * @param side A changed position's direction.
     * @param oldPrice An base position's price.
     * @param oldSize A base position's size.
     * @param newPrice An changed position's price.
     * @param newSize A changed position's size.
     */
    private void calculateCompoundPosition(Direction side, Num oldPrice, Num oldSize, Num newPrice, Num newSize) {
        Num diffSize = newSize.minus(oldSize);

        // compute compound size and price
        if (compoundSize.v.isZero()) {
            // no position
            if (side.isBuy()) {
                compoundSize.set(diffSize);
            } else {
                compoundSize.set(diffSize.negate());
            }
            compoundTotalPrice = newPrice.multiply(diffSize);
        } else {
            if (compoundSize.v.isPositive()) {
                // long position
                if (side.isBuy()) {
                    Num oldTotalPrice = oldPrice.multiply(oldSize);
                    Num newTotalPrice = newPrice.multiply(newSize);

                    compoundSize.set(v -> v.plus(diffSize));
                    compoundTotalPrice = compoundTotalPrice.minus(oldTotalPrice).plus(newTotalPrice);
                } else {
                    compoundSize.set(v -> v.minus(diffSize));
                    if (compoundSize.v.isNegative()) {
                        compoundTotalPrice = newPrice.multiply(compoundSize).abs();
                    } else {
                        compoundTotalPrice = compoundPrice.v.multiply(compoundSize);
                    }
                }
            } else {
                // short position
                if (side.isBuy()) {
                    compoundSize.set(v -> v.plus(diffSize));
                    if (compoundSize.v.isPositive()) {
                        compoundTotalPrice = newPrice.multiply(compoundSize);
                    } else {
                        compoundTotalPrice = compoundPrice.v.multiply(compoundSize).abs();
                    }
                } else {
                    Num oldTotalPrice = oldPrice.multiply(oldSize);
                    Num newTotalPrice = newPrice.multiply(newSize);

                    compoundSize.set(v -> v.minus(diffSize));
                    compoundTotalPrice = compoundTotalPrice.minus(oldTotalPrice).plus(newTotalPrice);
                }
            }
        }

        if (compoundSize.v.isZero()) {
            compoundPrice.set(Num.ZERO);
            compoundTotalPrice = Num.ZERO;
        } else {
            compoundPrice.set(compoundTotalPrice.divide(compoundSize.v.abs()).scale(service.setting.base.scale));
        }
    }

    /**
     * Stream for the current managed {@link Order}s and the incoming {@link Order}s.
     * 
     * @return
     */
    public Signal<Order> manages() {
        return I.signal(managed).merge(added);
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
                    .retry(service.retryPolicy(5))
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

            return service.cancel(order).retry(service.retryPolicy(5)).effectOnError(e -> {
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
            add.accept(order);

            // check order executions while request and response
            orders.forEach(e -> {
                if (e.id.equals(orderId)) {
                    updatePartially(order, e);
                }
            });

            // order termination will unregister
            order.observeTerminating().to(remove::accept);
        }
    }

    /**
     * Builder collections for the updating order.
     */
    public static class Update {
        /**
         * Create the updater to add the specified order.
         * 
         * @param id An order id that you want to add.
         * @param side A order's direction.
         * @param size A order's size.
         * @param price A order's price.
         * @return The updater.
         */
        public static Order create(String id, Direction side, Num size, Num price) {
            return Order.with.direction(side, size).price(price).id(id);
        }

        /**
         * Create the updater to cancle the specified order.
         * 
         * @param id An order id that you want to cancel.
         * @return The updater.
         */
        public static Order cancel(String id) {
            return Order.with.buy(Num.ONE).id(id).state(OrderState.CANCELED);
        }

        /**
         * Create the updater to execute the specified order.
         * 
         * @param id An order id that you want to execute.
         * @param size A order's total executed size.
         * @param price A order's average executed price.
         * @return The updater.
         */
        public static Order execute(String id, Num size, Num price, Num commission) {
            return Order.with.buy(size).price(price).commission(commission).id(id).state(OrderState.ACTIVE);
        }

        /**
         * Create the updater to execute the specified order.
         * 
         * @param id An order id that you want to execute.
         * @param size A order's current executed size.
         * @param price A order's current executed price.
         * @return The updater.
         */
        public static Order executePartially(String id, Num size, Num price, Num commission) {
            return Order.with.buy(size).price(price).commission(commission).id(id).state(OrderState.ACTIVE_PARTIAL);
        }
    }
}