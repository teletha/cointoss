/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.ticker.Span;
import cointoss.util.arithmetic.Num;
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

        /**
         * Use the chained strategy.
         * 
         * @param strategy
         * @return
         */
        OrderStrategy next(Consumer<Orderable> strategy);
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
        Orderable cancelAfter(long time, ChronoUnit unit);

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param duration A time until canceling.
         * @return
         */
        default Orderable cancelAfter(Duration duration) {
            return cancelAfter(duration.toSeconds(), ChronoUnit.SECONDS);
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param span A time until canceling.
         * @return
         */
        default Orderable cancelAfter(Span span) {
            return cancelAfter(span.duration);
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param <S>
         * @param timing A timing to cancel order.
         * @return
         */
        Orderable cancelWhen(Signal<?> timing);
    }
}