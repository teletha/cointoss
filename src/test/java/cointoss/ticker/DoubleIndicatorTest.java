/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Ⅱ;

class DoubleIndicatorTest extends TickerTestSupport {

    @Test
    void valueAt() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(ticker.ticks.at(sec * 0)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(sec * 1)) == 2d;
        assert indicator.valueAt(ticker.ticks.at(sec * 2)) == 3d;
        assert indicator.valueAt(ticker.ticks.at(sec * 3)) == 4d;
        assert indicator.valueAt(ticker.ticks.at(sec * 4)) == 5d;
    }

    @Test
    void valueAtLowerTick() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        Ticker lower = manager.on(Span.Minute1);
        long sec = lower.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(lower.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(lower.ticks.at(2 * sec)) == 1d;
        assert indicator.valueAt(lower.ticks.at(4 * sec)) == 1d;
        assert indicator.valueAt(lower.ticks.at(5 * sec)) == 2d;
        assert indicator.valueAt(lower.ticks.at(6 * sec)) == 2d;
        assert indicator.valueAt(lower.ticks.at(9 * sec)) == 2d;
        assert indicator.valueAt(lower.ticks.at(10 * sec)) == 3d;
        assert indicator.valueAt(lower.ticks.at(20 * sec)) == 5d;
    }

    @Test
    void combine() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Ⅱ<Double, Double>> indicator = open.combine(low);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).equals(I.pair(1d, 1d));
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).equals(I.pair(2d, 2d));
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).equals(I.pair(3d, 3d));
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).equals(I.pair(4d, 4d));
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).equals(I.pair(5d, 5d));
    }

    @Test
    void map() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        Indicator<Num> indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).map(v -> Num.of(v));
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).is(1);
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).is(2);
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).is(3);
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).is(4);
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).is(5);
    }

    @Test
    void mapWith() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> indicator = open.map(low, (o, l) -> Num.of(o).plus(l));
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).is(2);
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).is(4);
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).is(6);
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).is(8);
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).is(10);
    }

    @Test
    void mapWith2() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice);
        DoubleIndicator high = DoubleIndicator.build(ticker, tick -> tick.highPrice);
        Indicator<Num> indicator = open.map(low, high, (o, l, h) -> Num.of(o).plus(l).plus(h));
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).is(3);
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).is(6);
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).is(9);
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).is(12);
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).is(15);
    }

    @Test
    void mapToDouble() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).dmap(v -> v + 1);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 2d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 3d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 4d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 5d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 6d;
    }

    @Test
    void mapToDoubleWith() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice);
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice);
        DoubleIndicator indicator = open.dmap(low, (o, l) -> o + l);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 2d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 4d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 6d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 8d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 10d;
    }

    @Test
    void memo() {
        AtomicInteger count = new AtomicInteger();

        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> {
            count.incrementAndGet();
            return tick.openPrice;
        });

        DoubleIndicator memo = indicator.memoize();
        memo.valueAt(ticker.ticks.at(0 * sec));
        assert count.get() == 1;
        memo.valueAt(ticker.ticks.at(0 * sec));
        assert count.get() == 1;

        memo.valueAt(ticker.ticks.at(1 * sec));
        assert count.get() == 2;
        memo.valueAt(ticker.ticks.at(2 * sec));
        assert count.get() == 3;

        memo.valueAt(ticker.ticks.at(1 * sec));
        assert count.get() == 3;
        memo.valueAt(ticker.ticks.at(2 * sec));
        assert count.get() == 3;

        // from not memo
        indicator.valueAt(ticker.ticks.at(2 * sec));
        assert count.get() == 4;
        indicator.valueAt(ticker.ticks.at(2 * sec));
        assert count.get() == 5;
    }

    @Test
    void scale() {
        Ticker ticker = ticker(Span.Minute5, 1.23, 2.34, 3.45, 4.56, 5.67);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).scale(1);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1.2d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 2.3d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 3.4d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 4.6d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 5.7d;
    }

    @Test
    void sma() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).sma(2);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.5d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 3.5d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 4.5d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).sma(4);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 2.5d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 3.5d;
    }

    @Test
    void ema() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).ema(2);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.6666666666666665;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.5555555555555554d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 3.518518518518518d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 4.506172839506172d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).ema(4);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.4d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.04d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 2.824d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 3.6944d;
    }

    @Test
    void mma() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).mma(2);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.25d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 3.125d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 4.0625d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).mma(4);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.25d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 1.6875d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 2.265625d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 2.94921875d;
    }

    @Test
    void wma() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).wma(2);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 3.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 4.6666666666666667d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice).wma(4);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 1.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 2.3333333333333333d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 3d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 4d;
    }

    @Test
    void dontCacheLatest() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3);
        long sec = ticker.span.seconds;
        Tick tick2 = ticker.ticks.at(2 * sec);

        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.closePrice()).memoize();
        assert indicator.valueAt(tick2) == 3d;

        // update latest price
        manager.update(Execution.with.buy(1).price(10));
        assert indicator.valueAt(tick2) == 10d;

        // update latest price
        manager.update(Execution.with.buy(1).price(15));
        assert indicator.valueAt(tick2) == 15d;

        // step into next tick
        manager.update(Execution.with.buy(1).price(20).date(tick2.date().plusSeconds(Span.Minute5.seconds)));
        assert indicator.valueAt(tick2) == 15d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 20d;
    }
}