/*
 * Copyright (C) 2023 The COINTOSS Development Team
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseTriFunction;

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
        default Cancellable make(Num price) {
            return make((market, direction, size) -> price, "Make order at the specified price.");
        }

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
         * Build your limit order by the current market infomation.
         * 
         * @param price
         * @return
         */
        default Cancellable make(WiseTriFunction<Market, Direction, Num, Num> price) {
            return make(price, "Make order at the specified price.");
        }

        /**
         * Build your limit order by the current market infomation.
         * 
         * @param price
         * @return
         */
        Cancellable make(WiseTriFunction<Market, Direction, Num, Num> price, String description);

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @return Maker is cancellable.
         */
        default Cancellable makeBestPrice() {
            return make((market, direction, price) -> market.orderBook.by(direction)
                    .computeBestPrice(market.service.setting.base.minimumSize), "Make order at the best price.");
        }

        /**
         * Limit order with the best price by referrencing order books.
         * 
         * @param direction You can reference orderbook by direction.
         * @return Maker is cancellable.
         */
        default Cancellable makeBestPrice(Direction direction) {
            return make((market, d, price) -> market.orderBook.by(direction)
                    .computeBestPrice(market.service.setting.base.minimumSize), "Make order at the bast price by side.");
        }

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
         * @param time A time value.
         * @param unit A time unit.
         * @return
         */
        default Orderable cancelAfter(long time, ChronoUnit unit) {
            return cancelAfter(time, TimeUnit.of(unit));
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param time A time value.
         * @param unit A time unit.
         * @return
         */
        default Orderable cancelAfter(long time, TimeUnit unit) {
            return cancelWhen(scheduler -> I.schedule(time, unit, scheduler), "Cancel order after " + time + " " + unit + ".");
        }

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
         * @param timing A timing to cancel order.
         * @return
         */
        default Orderable cancelWhen(Signal<?> timing) {
            return cancelWhen(timing, "Cancel order when the specifid timing.");
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param timing A timing to cancel order.
         * @return
         */
        default Orderable cancelWhen(Signal<?> timing, String description) {
            return cancelWhen(scheduler -> timing, description);
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param timing A timing to cancel order.
         * @return
         */
        default Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing) {
            return cancelWhen(timing, "Cancel order when the specified timing.");
        }

        /**
         * Cancel the order if it remains after the specified time has passed.
         * 
         * @param timing A timing to cancel order.
         * @return
         */
        Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing, String description);
    }
}