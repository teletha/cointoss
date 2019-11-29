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

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Predicate;

import cointoss.Direction;
import cointoss.Market;
import cointoss.Scenario;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Signal;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.PlotScript;

/**
 * 
 */
public class VolumeCross extends Trader {

    Indicator<Num> priceDiff;

    public VolumeCross(Market market) {
        super(market);

        Indicator<Num> buyPriceIncrease = Indicator.build(market.tickers.of(Span.Minute5), Tick::buyPriceIncrease).sma(21);
        Indicator<Num> sellPriceDecrease = Indicator.build(market.tickers.of(Span.Minute5), Tick::sellPriceDecrease).sma(21);
        priceDiff = buyPriceIncrease.map(sellPriceDecrease, (b, s) -> b.minus(s)).scale(market.service.setting.targetCurrencyScaleSize);

        // disableWhile(observeProfit().map(p -> p.isLessThan(-10000)));

        double size = 0.3;

        when(priceDiff.observe().plug(near(5, o -> o.isGreaterThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, o -> o.make(market.tickers.latestPrice.v.minus(300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(priceDiff.observe().plug(near(2, o -> o.isLessThan(0))), o -> o.take());
            }
        });

        when(priceDiff.observe().plug(near(5, o -> o.isLessThan(0))), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.SELL, size, o -> o.make(market.tickers.latestPrice.v.plus(300)).cancelAfter(3, MINUTES));
            }

            @Override
            protected void exit() {
                exitWhen(priceDiff.observe().plug(near(2, o -> o.isGreaterThan(0))), o -> o.take());
            }
        });
    }

    private <In> Function<Signal<In>, Signal<List<In>>> near(int size, Predicate<In> condition) {
        return signal -> signal.buffer(size, 1).take(buff -> buff.stream().allMatch(condition));
    }

    /**
     * 
     */
    class Plot extends PlotScript implements StyleDSL {

        Style diff = () -> {
            stroke.color("#eee");
        };

        @Override
        protected void declare(Market market, Ticker ticker) {
            bottomN.line(priceDiff, diff);
        }
    }
}