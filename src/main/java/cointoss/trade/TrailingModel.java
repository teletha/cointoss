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

import java.util.function.Function;

import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;
import kiss.Signal;

@Icy
abstract class TrailingModel {

    /**
     * Setting the losscut price range.
     * 
     * @return
     */
    @Icy.Property
    public abstract Num losscut();

    @Icy.Overload("losscut")
    private Num losscut(double price) {
        return Num.of(price);
    }

    @Icy.Overload("losscut")
    private Num losscut(long price) {
        return Num.of(price);
    }

    @Icy.Intercept("losscut")
    private Num losscut(Num price) {
        if (price.isLessThanOrEqual(0)) {
            throw new IllegalArgumentException("Lower price range must be positive.");
        }
        return price;
    }

    /**
     * Setting the profit price range.
     * 
     * @return
     */
    @Icy.Property
    public Num profit() {
        return Num.ZERO;
    }

    @Icy.Overload("profit")
    private Num profit(double price) {
        return Num.of(price);
    }

    @Icy.Overload("profit")
    private Num profit(long price) {
        return Num.of(price);
    }

    /**
     * Setting the price update timing
     * 
     * @return
     */
    @Icy.Property
    public Function<Market, Signal<Num>> update() {
        return update(Span.Minute1);
    }

    @Icy.Overload("update")
    private Function<Market, Signal<Num>> update(Span span) {
        return market -> market.tickers.on(span).open.map(tick -> Num.of(tick.openPrice));
    }
}