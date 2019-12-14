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

import java.time.temporal.ChronoUnit;

import cointoss.Direction;
import cointoss.FundManager;
import cointoss.Market;
import cointoss.Scenario;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.Indicators;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.util.Num;
import kiss.Ⅱ;

public class LazyBear extends Trader {

    public TimeSpan tickerSpan = TimeSpan.Second15;

    public int entryThreshold = 50;

    public int exitThreshold = 5;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker ticker = market.tickers.of(tickerSpan);
        Indicator<Ⅱ<Num, Num>> oscillator = Indicators.waveTrend(ticker);

        // disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.1;

        when(oscillator.observeWhen(ticker.add).take(v -> v.ⅰ.isLessThan(-entryThreshold)), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observeWhen(ticker.add).take(v -> v.ⅰ.isGreaterThan(exitThreshold)), s -> s.take());
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this, 3000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 7000)).to());
            }
        });

        when(oscillator.observeWhen(ticker.add).take(v -> v.ⅰ.isGreaterThan(entryThreshold)), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observeWhen(ticker.add).take(v -> v.ⅰ.isLessThan(-exitThreshold)), s -> s.take());
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this, 3000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 7000)).to());
            }
        });
    }
}