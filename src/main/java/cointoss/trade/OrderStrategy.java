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
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.Market;
import cointoss.order.Order;
import cointoss.util.Num;
import cointoss.util.PentaConsumer;
import kiss.I;

public interface OrderStrategy {

    /**
     * Taker order strategy.
     */
    public interface Takable extends OrderStrategy {

        /**
         * Market order.
         * 
         * @return Taker is NOT cancellable.
         */
        OrderStrategy take();
    }

    /**
     * Maker order strategy.
     */
    public interface Makable extends OrderStrategy {

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        default Cancellable make(long price) {
            return make(Num.of(price));
        }

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        default Cancellable make(double price) {
            return make(Num.of(price));
        }

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        Cancellable make(Num price);

        /**
         * Limit order with the best limit price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        Cancellable makeBestPrice();
    }

    /**
     * Cancelling order strategy.
     */
    public static interface Cancellable extends OrderStrategy {

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param <S>
         * @param time A time value.
         * @param unit A time unit.
         * @return
         */
        <S extends Takable & Makable> S cancelAfter(long time, ChronoUnit unit);
    }

    /**
     * Helper to build various {@link OrderStrategy}.
     */
    class with {

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        public static Cancellable make(long price) {
            return make(Num.of(price));
        }

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        public static Cancellable make(double price) {
            return make(Num.of(price));
        }

        /**
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        public static Cancellable make(Num price) {
            return new OrderStrategyImpl().make(price);
        }

        /**
         * Limit order with the best limit price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        public static Cancellable makeLowest() {
            return new OrderStrategyImpl().makeBestPrice();
        }

        /**
         * Market order.
         * 
         * @return Taker is NOT cancellable.
         */
        public static OrderStrategy take() {
            return new OrderStrategyImpl().take();
        }

        /**
         * 
         */
        static class OrderStrategyImpl implements Takable, Makable, Cancellable {

            /** The action sequence. */
            LinkedList<PentaConsumer<Market, Direction, Num, Order, Consumer<Order>>> actions = new LinkedList();

            /**
             * {@inheritDoc}
             */
            @Override
            public OrderStrategy take() {
                actions.add((market, direction, size, previous, orders) -> {
                    Order order = Order.with.direction(direction, size);
                    orders.accept(order);

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
            public Cancellable make(Num price) {
                actions.add((market, direction, size, previous, orders) -> {
                    Order order = Order.with.direction(direction, size).price(price);
                    orders.accept(order);

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
            public Cancellable makeBestPrice() {
                actions.add((market, direction, size, previous, orders) -> {
                    make(market.orderBook.bookFor(direction).best.v.price);
                    execute(market, direction, size, previous, orders);
                });
                return this;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <S extends Takable & Makable> S cancelAfter(long time, ChronoUnit unit) {
                actions.add((market, direction, size, previous, orders) -> {
                    if (previous != null && previous.isNotCompleted()) {
                        I.schedule(time, TimeUnit.of(unit), () -> {
                            if (previous.isNotCompleted()) {
                                market.orders.cancel(previous).to(() -> {
                                    execute(market, direction, size, null, orders);
                                });
                            }
                        });
                    }
                });
                return (S) this;
            }

            /**
             * Execute next order action.
             * 
             * @param market
             * @param direction
             * @param size
             */
            void execute(Market market, Direction direction, Num size, Order previous, Consumer<Order> orders) {
                PentaConsumer<Market, Direction, Num, Order, Consumer<Order>> action = actions.pollFirst();

                if (action != null) {
                    action.accept(market, direction, size, previous, orders);
                }
            }
        }
    }
}
