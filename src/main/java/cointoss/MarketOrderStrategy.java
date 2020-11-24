/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.order.Order;
import cointoss.order.OrderStrategy;
import cointoss.order.OrderStrategy.Cancellable;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.arithmetic.Num;
import kiss.Observer;
import kiss.Signal;
import kiss.WiseTriFunction;

/**
 * 
 */
class MarketOrderStrategy implements Orderable, Takable, Makable, Cancellable {

    /** The action sequence. */
    final LinkedList<DescriptableOrderAction> actions = new LinkedList();

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStrategy next(Consumer<Orderable> strategy) {
        if (strategy != null) {
            actions.add(new DescriptableOrderAction("", (market, direction, size, previous, orders) -> {
                strategy.accept(this);
                execute(market, direction, size, previous, orders);
            }));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStrategy take() {
        actions.add(new DescriptableOrderAction("Take order.", (market, direction, size, previous, orders) -> {
            Order order = Order.with.direction(direction, size);
            orders.accept(order);

            market.orders.request(order).to(() -> {
                execute(market, direction, size, order, orders);
            });
        }));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cancellable make(WiseTriFunction<Market, Direction, Num, Num> price, String description) {
        actions.add(new DescriptableOrderAction(description, (market, direction, size, previous, orders) -> {
            Order order = Order.with.direction(direction, size).price(price.apply(market, direction, size));
            orders.accept(order);

            market.orders.request(order).to(() -> {
                execute(market, direction, size, order, orders);
            });
        }));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing, String description) {
        actions.add(new DescriptableOrderAction(description, (market, direction, size, previous, orders) -> {
            if (previous != null && previous.isNotCompleted()) {
                timing.apply(market.service.scheduler()).first().to(() -> {
                    if (previous.isNotCompleted()) {
                        market.orders.cancel(previous).to(() -> {
                            if (previous.remainingSize().isPositive()) {
                                execute(market, direction, previous.remainingSize(), null, orders);
                            }
                        });
                    }
                });
            }
        }));
        return this;
    }

    /**
     * Execute next order action.
     * 
     * @param market
     * @param direction
     * @param size
     */
    void execute(Market market, Direction direction, Num size, Order previous, Observer<? super Order> observer) {
        DescriptableOrderAction order = actions.pollFirst();

        if (order == null) {
            observer.complete();
        } else {
            order.action.execute(market, direction, size, previous, observer);
        }
    }

    private class DescriptableOrderAction {

        private final String description;

        private final OrderAction action;

        /**
         * @param description
         * @param action
         */
        public DescriptableOrderAction(String description, OrderAction action) {
            this.description = description;
            this.action = action;
        }
    }

    /**
     * Internal API.
     */
    private interface OrderAction {

        /**
         * Execute this order action.
         * 
         * @param market A target {@link Market}.
         * @param direction A order's {@link Direction}.
         * @param num A order size.
         * @param order The previous order.
         * @param observer A event observer.
         */
        void execute(Market market, Direction direction, Num num, Order order, Observer<? super Order> observer);
    }
}