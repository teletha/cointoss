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
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.util.Num;

/**
 * 
 */
public class VolumeCross extends Trader {

    Indicator<Num> buy = Indicator.build(market.tickers.of(Span.Second15), Tick::buyVolume).ema(21);

    Indicator<Num> sell = Indicator.build(market.tickers.of(Span.Second15), Tick::sellVolume).ema(21);

    Indicator<Num> buyPriceIncrease = Indicator.build(market.tickers.of(Span.Second15), Tick::buyPriceIncrease);

    Indicator<Num> sellPriceDecrease = Indicator.build(market.tickers.of(Span.Second15), Tick::sellPriceDecrease);

    Indicator<Num> diff = buy.map(sell, (b, s) -> b.minus(s));

    public VolumeCross(Market market) {
        super(market);

        // disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.1;

        when(diff.observe().take(v -> v.isGreaterThan(0)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, o -> o.make(market.tickers.latestPrice.v.minus(300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(diff.observe().take(v -> v.isLessThan(-2)), o -> o.take());
            }
        });

        when(diff.observe().take(v -> v.isLessThan(0)), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, o -> o.make(market.tickers.latestPrice.v.plus(300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(diff.observe().take(v -> v.isGreaterThan(2)), o -> o.take());
            }
        });
    }
}