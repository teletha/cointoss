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
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Ⅱ;
import stylist.Style;
import trademate.chart.ChartStyles;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class LazyBear extends Trader {

    public TimeSpan tickerSpan = TimeSpan.Second15;

    public int entryThreshold = 50;

    public int exitThreshold = 5;

    public int decay = 10;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker sec5 = market.tickers.on(TimeSpan.Second5);

        NumIndicator buys = NumIndicator.build(sec5, v -> Num.of(v.buyVolume())).ema(10);
        NumIndicator sells = NumIndicator.build(sec5, v -> Num.of(v.sellVolume())).ema(10);

        Signal<Boolean> mustBuy = buys.map(sells, (b, s) -> b.isGreaterThan(s)).observeWhen(sec5.close);
        Signal<Boolean> mustSell = mustBuy.map(v -> !v);

        Ticker ticker = market.tickers.on(tickerSpan);
        Indicator<Ⅱ<Num, Num>> oscillator = Indicators.waveTrend(ticker);

        double size = 0.1;

        when(oscillator.observeWhen(ticker.close).take(v -> v.ⅰ.isLessThan(-entryThreshold)).skipWhile(mustSell), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observeWhen(ticker.close).take(v -> v.ⅰ.isGreaterThan(exitThreshold)), s -> s.take());
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

        when(oscillator.observeWhen(ticker.close).take(v -> v.ⅰ.isGreaterThan(entryThreshold)).skipWhile(mustBuy), value -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, size, s -> s.make(market.tickers.latestPrice.v.minus(this, 300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(oscillator.observeWhen(ticker.close).take(v -> v.ⅰ.isLessThan(-exitThreshold)), s -> s.take());
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
                    line(buys, Long);
                    line(sells, Short);
                });
            }
        });
    }
}