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
import cointoss.analyze.PrimitiveStats;
import cointoss.ticker.Indicator;
import cointoss.ticker.Indicators;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.util.Num;
import kiss.Variable;
import kiss.Ⅱ;
import stylist.Style;
import trademate.chart.ChartStyles;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class LazyBear extends Trader {

    public TimeSpan tickerSpan = TimeSpan.Second15;

    public int entryThreshold = 35;

    public int exitThreshold = 15;

    public double decay = 0.95;

    public double diff = 0.1;

    public int losscutRange = 3000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Variable<Boolean> mustBuy = Variable.of(false);
        Variable<Boolean> mustSell = Variable.of(false);
        PrimitiveStats buys = new PrimitiveStats().decay(decay);
        PrimitiveStats sells = new PrimitiveStats().decay(decay);

        market.tickers.on(TimeSpan.Second5).close.to(tick -> {
            buys.add(tick.buyVolume());
            sells.add(tick.sellVolume());
            double bm = buys.mean();
            double sm = sells.mean();
            double ratio = bm / sm;

            mustBuy.set(1d + diff < ratio);
            mustSell.set(1d - diff < ratio);
        });

        Ticker ticker = market.tickers.on(tickerSpan);
        Indicator<Ⅱ<Num, Num>> oscillator = Indicators.waveTrend(ticker);

        double size = 0.1;

        when(oscillator.observeWhen(ticker.open).take(v -> v.ⅰ.isLessThan(-entryThreshold)).skip(v -> mustSell.v), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.minus(direction(), losscutRange));
                exitWhen(oscillator.observeWhen(ticker.open).take(v -> v.ⅰ.isGreaterThan(exitThreshold)), s -> s.take());
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

        when(oscillator.observeWhen(ticker.open)
                .take(v -> v.ⅰ.isGreaterThan(entryThreshold))
                .skip(v -> mustBuy.v), value -> new Scenario() {

                    @Override
                    protected void entry() {
                        entry(Direction.SELL, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300))
                                .cancelAfter(3, ChronoUnit.MINUTES));
                    }

                    @Override
                    protected void exit() {
                        exitAt(entryPrice.minus(direction(), losscutRange));
                        exitWhen(oscillator.observeWhen(ticker.open).take(v -> v.ⅰ.isLessThan(-exitThreshold)), s -> s.take());
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

            public Style Long = () -> {
                stroke.color(ChartStyles.buy);
            };

            public Style Short = () -> {
                stroke.color(ChartStyles.sell);
            };

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
}