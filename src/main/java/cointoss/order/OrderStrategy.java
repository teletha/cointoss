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

import cointoss.util.Num;

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
}
