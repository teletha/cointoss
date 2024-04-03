/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Direction;
import cointoss.MarketService;
import hypatia.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;

/**
 * 
 */
public final class OrderManager {

    /** The actual service. */
    private final MarketService service;

    /** The actual order manager. */
    private final Map<String, Order> managed = new ConcurrentHashMap();

    /** The unmodifiable exposed active orders. */
    public final Map<String, Order> items = Collections.unmodifiableMap(managed);

    /**
     * @param service
     */
    public OrderManager(MarketService service) {
        this.service = service;

        // retrieve orders on server
        // don't use orders().to(addition); it completes addition signaling itself
        service.orders(OrderState.ACTIVE).to(this::update);

        // retrieve orders on realtime
        service.add(service.ordersRealtimely().to(this::update));
    }

    /**
     * Update order.
     * 
     * @param updater
     */
    final void update(Order updater) {
        Order manage = managed.get(updater.id);
        if (manage == null) {
            switch (updater.state) {
            case INIT:
                add(updater);
                return;

            default:
                return;
            }
        } else {
            switch (updater.state) {
            case CANCELED:
                cancel(manage, updater);
                return;

            case ACTIVE:
                updateFully(manage, updater);
                return;

            case ACTIVE_PARTIAL:
                updatePartially(manage, updater);
                return;

            default:
                return;
            }
        }
    }

    /**
     * Add new manageable order.
     * 
     * @param order
     */
    private void add(Order order) {
        order.setState(ACTIVE);

        // add order
        managed.put(order.id, order);

        // unregister when terminating
        order.observeTerminating().first().to(() -> {
            // remove order
            managed.remove(order.id);
        });
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

            Builder complementer = new Builder(order);

            return service.request(order)
                    .effectOnObserve(complementer::start)
                    .effect(complementer::check)
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
     * Build the {@link Signal} which requests all specified {@link Order} to the market. This
     * method DON'T request order, you MUST subscribe {@link Signal}. If you want to request
     * actually, you can use {@link #requestNow(Order)}.
     * 
     * @param orders A list of order to request.
     * @return A order request process.
     * @see #requestNow(List)
     */
    public Signal<Order> request(List<Order> orders) {
        return I.signal(orders).flatMap(this::request);
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
     * Request all specified {@link Order} to the market actually.
     * 
     * @param orders A list of order to request.
     * @return A list of requested {@link Order}.
     * @see #request(Order)
     */
    public List<Order> requestNow(List<Order> orders) {
        request(orders).to(I.NoOP);

        return orders;
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

            return service.cancel(order).effectOnError(e -> {
                order.setState(previous);
            });
        } else {
            return I.signal(order);
        }
    }

    /**
     * Build the {@link Signal} which cancels all specified {@link Order} from the market. This
     * method DON'T cancel order, you MUST subscribe {@link Signal}. If you want to cancel actually,
     * you can use {@link #cancelNow(Order)}.
     * 
     * @param orders A list of orders to cancel.
     * @return A order cancel process.
     * @see #cancelNow(List)
     */
    public Signal<Order> cancel(List<Order> orders) {
        return I.signal(orders).flatMap(this::cancel);
    }

    public Signal<Boolean> cancelAll() {
        return service.cancelAll();
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
     * Cancel all specified {@link Order} from the market actually.
     * 
     * @param orders A list of orders to cancel.
     * @return A list of canceled {@link Order}.
     * @see #cancel(List)
     */
    public List<Order> cancelNow(List<Order> orders) {
        cancel(orders).to(I.NoOP);
        return orders;
    }

    /**
     * Cancel all orders.
     */
    public void cancelAllNow() {
        service.cancelAll().to(I.NoOP);
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
     * Support executions while order request and response. If the execution data comes to the
     * real-time API before the oreder's response, the order cannot be identified from the real-time
     * API.
     * 
     * Record all execution data from request to response, and check if there is already an
     * execution data at response.
     */
    private class Builder {

        /** The associated order. */
        private final Order order;

        /** The realtime order manager. */
        private final LinkedList<Order> orders = new LinkedList();

        /** The disposer for realtime execution stream. */
        private Disposable disposer;

        /**
         * 
         */
        private Builder(Order order) {
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
        private void check(String orderId) {
            // stop recording realtime executions and register order id atomically
            disposer.dispose();
            order.setId(orderId);
            add(order);

            // check order executions while request and response
            for (Order o : orders) {
                if (o.id.equals(orderId)) {
                    switch (o.state) {
                    case ACTIVE:
                    case ACTIVE_PARTIAL:
                    case COMPLETED:
                        updatePartially(order, o);
                        break;

                    default:
                        break;
                    }
                }
            }
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
            return Order.with.orientation(side, size).price(price).id(id);
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