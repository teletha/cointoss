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
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Predicate;

import cointoss.Direction;
import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.trade.FundManager;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.util.Num;
import kiss.Signal;
import stylist.Style;
import trademate.TradeMateStyle;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

/**
 * 
 */
public class VolumeCross extends Trader {

    public int smaLength = 3;

    public TimeSpan span = TimeSpan.Minute5;

    public int profitRange = 5000;

    public int losscutRange = 5000;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker ticker = market.tickers.on(span);
        Indicator<Double> buyVolume = Indicator.build(ticker, Tick::buyVolume);
        Indicator<Double> sellVolume = Indicator.build(ticker, Tick::sellVolume);
        NumIndicator volumeDiff = buyVolume.nmap(sellVolume, (b, s) -> Num.of(b - s))
                .sma(7)
                .scale(market.service.setting.targetCurrencyScaleSize);

        Indicator<Boolean> upPrediction = Indicator.build(ticker, Tick::isBear).map(volumeDiff, (t, d) -> t && d.isPositive());
        Indicator<Boolean> downPrediction = Indicator.build(ticker, Tick::isBull).map(volumeDiff, (t, d) -> t && d.isNegative());

        // disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.3;

        when(volumeDiff.observeWhen(ticker.close).plug(near(5, o -> o.isGreaterThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, o -> o.make(market.tickers.latestPrice.v.minus(300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(direction(), profitRange));
                exitAt(entryPrice.minus(direction(), losscutRange));
                exitWhen(volumeDiff.observeWhen(ticker.close).plug(near(2, o -> o.isLessThan(0))), o -> o.take());
            }
        });

        when(volumeDiff.observeWhen(ticker.close).plug(near(5, o -> o.isLessThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size, o -> o.make(market.tickers.latestPrice.v.plus(300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(direction(), profitRange));
                exitAt(entryPrice.minus(direction(), losscutRange));
                exitWhen(volumeDiff.observeWhen(ticker.close).plug(near(2, o -> o.isGreaterThan(0))), o -> o.take());
            }
        });

        option(new PlotScript() {
            Style diff = () -> {
                stroke.color("#eee");
            };

            Style upMark = () -> {
                fill.color(TradeMateStyle.BUY);
            };

            Style downMark = () -> {
                fill.color(TradeMateStyle.SELL);
            };

            @Override
            protected void declare(Market market, Ticker ticker) {
                mark(upPrediction, upMark);
                mark(downPrediction, downMark);

                in(PlotArea.LowNarrow, () -> {
                    line(volumeDiff, diff);
                });
            }
        });
    }

    private <In> Function<Signal<In>, Signal<List<In>>> near(int size, Predicate<In> condition) {
        return signal -> signal.buffer(size, 1).take(buff -> buff.stream().allMatch(condition));
    }
}