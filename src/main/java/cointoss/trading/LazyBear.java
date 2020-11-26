/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trading;

import java.time.temporal.ChronoUnit;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Indicators;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.trade.Funds;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartStyles;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class LazyBear extends Trader {

    public Span tickerSpan = Span.Minute1;

    public int entryThreshold = 35;

    public int exitThreshold = 15;

    public double decay = 0.95;

    public double diff = 0.1;

    public int losscutRange = 3000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        Ticker ticker = market.tickers.on(tickerSpan);
        DoubleIndicator oscillator = Indicators.waveTrend(ticker);

        double size = 0.1;

        when(oscillator.valueAt(ticker.open).take(v -> v < -entryThreshold), value -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, s -> s.make(market.tickers.latest.v.price.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.minus(direction(), losscutRange));
                exitWhen(oscillator.valueAt(ticker.open).take(v -> v > exitThreshold), s -> s.take());
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this,
                // 3000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 7000)).to());
            }
        });

        when(oscillator.valueAt(ticker.open).take(v -> v > entryThreshold), value -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size, s -> s.make(market.tickers.latest.v.price.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.minus(direction(), losscutRange));
                exitWhen(oscillator.valueAt(ticker.open).take(v -> v < -exitThreshold), s -> s.take());
                // exitAt(market.tickers.of(Span.Second5).add.flatMap(tick -> {
                // if (tick.openPrice.isGreaterThan(this, entryPrice.plus(this,
                // 3000))) {
                // return I.signal(entryPrice.plus(this, 100));
                // } else {
                // return I.signal();
                // }
                // }).first().startWith(entryPrice.minus(this, 7000)).to());
            }
        });

        option(new PlotScript() {

            /**
             * {@inheritDoc}
             */
            @Override
            public String name() {
                return super.name() + "(" + decay + ")";
            }

            @Override
            protected void declare(Market market, Ticker ticker) {
                in(PlotArea.Bottom, () -> {
                });
            }
        });
    }

    interface style extends StyleDSL {
        Style Long = () -> {
            stroke.color(ChartStyles.buy);
        };

        Style Short = () -> {
            stroke.color(ChartStyles.sell);
        };
    }
}