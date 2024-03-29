/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.order.Cancellable;
import cointoss.order.Makable;
import cointoss.order.Order;
import cointoss.order.Orderable;
import cointoss.order.Takable;
import hypatia.Num;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.WiseConsumer;
import kiss.WiseRunnable;
import kiss.WiseTriFunction;

class OrderStrategy implements Orderable, Takable, Makable, Cancellable {

    /** The action sequence. */
    private final LinkedList<OrderAction> actions = new LinkedList();

    /**
     * {@inheritDoc}
     */
    @Override
    public Orderable next(Consumer<Orderable> strategy) {
        if (strategy != null) {
            actions.add((market, direction, size, previous, orders) -> {
                strategy.accept(this);
                execute(market, direction, size, previous, orders);
            });
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Orderable take() {
        actions.add((market, direction, size, previous, orders) -> {
            List<Order> order = I.list(Order.with.direction(direction, size));
            order.forEach(orders::accept);

            market.orders.request(order).to(() -> {
                execute(market, direction, size, order, orders);
            });
        });
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cancellable makeOrder(WiseTriFunction<Market, Direction, Num, List<Order>> price) {
        actions.add((market, direction, size, previous, orders) -> {
            List<Order> order = price.apply(market, direction, size);
            order.forEach(orders::accept);

            market.orders.request(order).last().to(() -> {
                execute(market, direction, size, order, orders);
            });
        });
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing, String description) {
        actions.add((market, direction, size, previous, orders) -> {
            processIncompleted(previous, () -> {
                timing.apply(market.service.scheduler()).first().to(() -> {
                    processIncompleted(previous, () -> {
                        market.orders.cancel(previous).last().to(() -> {
                            processRemaining(previous, remaining -> {
                                execute(market, direction, remaining, previous, orders);
                            });
                        });
                    });
                });
            });
        });
        return this;
    }

    private void processIncompleted(List<Order> orders, WiseRunnable process) {
        orders.removeIf(Order::isCompleted);

        if (!orders.isEmpty()) {
            process.run();
        }
    }

    private void processRemaining(List<Order> orders, WiseConsumer<Num> process) {
        orders.removeIf(o -> o.remainingSize().isNegativeOrZero());

        if (!orders.isEmpty()) {
            I.signal(orders).scan(() -> Num.ZERO, (x, o) -> x.plus(o.remainingSize())).last().to(process::accept);
        }
    }

    /**
     * Execute next order action.
     * 
     * @param market
     * @param direction
     * @param size
     */
    void execute(Market market, Direction direction, Num size, List<Order> previous, Observer<? super Order> observer) {
        OrderAction order = actions.pollFirst();

        if (order == null) {
            observer.complete();
        } else {
            order.execute(market, direction, size, previous, observer);
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
        void execute(Market market, Direction direction, Num num, List<Order> order, Observer<? super Order> observer);
    }
}