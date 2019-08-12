/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Variable;

public interface OrderStrategy {

    /**
     * Make stop order with waiting time.
     * 
     * @param time
     * @param unit
     * @return
     */
    static Consumer<Orderable> stop(long time, ChronoUnit unit) {
        return s -> {
            s.makeBestPrice().cancelAfter(time, unit).take();
        };
    }

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
         * Limit order with the specified price.
         * 
         * @param price A limit price.
         * @return Maker is cancellable.
         */
        default Cancellable make(Variable<Num> price) {
            return make(price.v);
        }

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        Cancellable makeBestPrice();

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        Cancellable makeBestPrice(Direction direction);

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        default Cancellable makeBestSellPrice() {
            return makeBestPrice(Direction.SELL);
        }

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        default Cancellable makeBestBuyPrice() {
            return makeBestPrice(Direction.BUY);
        }

        /**
         * Limit order with the current position price.
         * 
         * @return Maker is cancellable.
         */
        Cancellable makePositionPrice();
    }

    /**
     * Both order strategy.
     */
    public static interface Orderable extends Takable, Makable {
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

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param <S>
         * @param timing A timing to cancel order.
         * @return
         */
        <S extends Takable & Makable> S cancelWhen(Signal<?> timing);
    }
}
