/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import cointoss.Directional;
import cointoss.Market;
import cointoss.order.Orderable;
import cointoss.ticker.Span;
import hypatia.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

public interface TradingExit extends Directional {

    /**
     * The current market.
     * 
     * @return
     */
    Market market();

    /**
     * The averaged entry price.
     * 
     * @return
     */
    Num entryPrice();

    /**
     * The entry size.
     * 
     * @return
     */
    Num entrySize();

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param span
     */
    default void exitAfter(Span span) {
        exitAfter(span.seconds, TimeUnit.SECONDS);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param time
     * @param unit
     */
    default void exitAfter(long time, TimeUnit unit) {
        exitWhen(I.schedule(time, 0, unit, false, market().service.scheduler()).first(), Orderable::take);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(long price) {
        exitAt(Num.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(double price) {
        exitAt(Num.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(Num price) {
        exitAt(Variable.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(Variable<Num> price) {
        exitAt(price, entryPrice().isLessThan(this, price) ? s -> s.make(price) : s -> s.take());
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(Trailing price) {
        exitAt(price, Orderable::take);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(long price, Consumer<Orderable> strategy) {
        exitAt(Num.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(double price, Consumer<Orderable> strategy) {
        exitAt(Num.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(Num price, Consumer<Orderable> strategy) {
        exitAt(Variable.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    void exitAt(Variable<Num> price, Consumer<Orderable> strategy);

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    default void exitAt(Trailing price, Consumer<Orderable> strategy) {
        Num max = entryPrice().plus(this, price.profit);
        Variable<Num> trailedPrice = Variable.of(entryPrice().minus(this, price.losscut));

        price.update.apply(market()).to(current -> {
            Num trailing = Num.max(this, trailedPrice.v, current.minus(this, price.losscut));
            trailedPrice.set(Num.min(this, trailing, max));
        });

        exitAt(trailedPrice, strategy);
    }

    /**
     * Exit on your timing by full taker.
     * 
     * @param timing The exit timing.
     */
    default void exitWhen(Signal<?> timing) {
        exitWhen(timing, Orderable::take);
    }

    /**
     * Declare exit order.
     * 
     * @param timing The exit timing.
     * @param strategy The exit strategy.
     */
    public void exitWhen(Signal<?> timing, Consumer<Orderable> strategy);
}