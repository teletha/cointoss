/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trading;

import static java.time.temporal.ChronoUnit.MINUTES;

import cointoss.Direction;
import cointoss.Market;
import cointoss.Scenario;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.Indicators;
import cointoss.ticker.Span;
import cointoss.util.Num;
import kiss.Ⅱ;

/**
 * 
 */
public class LazyBear extends Trader {

    Indicator<Ⅱ<Num, Num>> oscillator = Indicators.waveTrend(market.tickers.of(Span.Second15));

    public LazyBear(Market market) {
        super(market);

        disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.1;

        when(oscillator.observe().take(v -> v.ⅰ.isLessThan(-50)), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observe().take(v -> v.ⅰ.isGreaterThan(value.ⅰ.negate().divide(2))), s -> s.take());
                exitAt(entryPrice.minus(this, 4900));
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this, 4000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 2000)).to());
            }
        });

        when(oscillator.observe().take(v -> v.ⅰ.isGreaterThan(50)), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observe().take(v -> v.ⅰ.isLessThan(value.ⅰ.negate().divide(2))), s -> s.take());
                exitAt(entryPrice.minus(this, 4900));
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this, 4000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 2000)).to());
            }
        });
    }
}