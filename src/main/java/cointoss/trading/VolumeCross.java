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
import cointoss.FundManager;
import cointoss.Market;
import cointoss.Scenario;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import cointoss.util.Num;
import kiss.Signal;
import stylist.Style;
import trademate.TradeMateStyle;
import trademate.chart.PlotScript;

/**
 * 
 */
public class VolumeCross extends Trader {

    public int smaLength = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
        Ticker ticker = market.tickers.of(TimeSpan.Minute5);
        Indicator<Double> buyVolume = Indicator.build(ticker, Tick::buyVolume);
        Indicator<Double> sellVolume = Indicator.build(ticker, Tick::sellVolume);
        NumIndicator volumeDiff = buyVolume.nmap(sellVolume, (b, s) -> Num.of(b - s))
                .sma(7)
                .scale(market.service.setting.targetCurrencyScaleSize);
        Indicator<Boolean> upPrediction = Indicator.build(ticker, Tick::isBear).map(volumeDiff, (t, d) -> t && d.isPositive());
        Indicator<Boolean> downPrediction = Indicator.build(ticker, Tick::isBull).map(volumeDiff, (t, d) -> t && d.isNegative());

        // disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.3;

        when(volumeDiff.observeWhen(ticker.add).plug(near(5, o -> o.isGreaterThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, o -> o.make(market.tickers.latestPrice.v.minus(300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(volumeDiff.observeWhen(ticker.add).plug(near(2, o -> o.isLessThan(0))), o -> o.take());
            }
        });

        when(volumeDiff.observeWhen(ticker.add).plug(near(5, o -> o.isLessThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size, o -> o.make(market.tickers.latestPrice.v.plus(300)).cancelAfter(3, ChronoUnit.MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(volumeDiff.observeWhen(ticker.add).plug(near(2, o -> o.isGreaterThan(0))), o -> o.take());
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
                lowN.line(volumeDiff, diff);
                main.mark(upPrediction, upMark);
                main.mark(downPrediction, downMark);
            }
        });
    }

    private <In> Function<Signal<In>, Signal<List<In>>> near(int size, Predicate<In> condition) {
        return signal -> signal.buffer(size, 1).take(buff -> buff.stream().allMatch(condition));
    }
}