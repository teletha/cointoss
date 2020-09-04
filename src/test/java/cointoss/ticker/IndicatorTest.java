/*
 * Copyright (C) 2020 cointoss Development Team
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

class IndicatorTest extends TickerTestSupport {

    @Test
    void valueAt() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).is(1);
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).is(2);
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).is(3);
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).is(4);
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).is(5);
    }

    @Test
    void valueAtLowerTick() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        Ticker lower = manager.on(Span.Minute1);
        long sec = lower.span.seconds;
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(lower.ticks.at(0 * sec)).is(1);
        assert indicator.valueAt(lower.ticks.at(2 * sec)).is(1);
        assert indicator.valueAt(lower.ticks.at(4 * sec)).is(1);
        assert indicator.valueAt(lower.ticks.at(5 * sec)).is(2);
        assert indicator.valueAt(lower.ticks.at(6 * sec)).is(2);
        assert indicator.valueAt(lower.ticks.at(9 * sec)).is(2);
        assert indicator.valueAt(lower.ticks.at(10 * sec)).is(3);
        assert indicator.valueAt(lower.ticks.at(20 * sec)).is(5);
    }

    @Test
    void combine() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        Indicator<Double> volume = Indicator.build(ticker, tick -> tick.longVolume());
        Indicator<Integer> low = Indicator.build(ticker, tick -> tick.lowPrice.intValue());
        Indicator<Ⅱ<Double, Integer>> indicator = volume.combine(low);
        assert indicator.valueAt(ticker.ticks.at(0 * sec)).equals(I.pair(1d, 1));
        assert indicator.valueAt(ticker.ticks.at(1 * sec)).equals(I.pair(2d, 2));
        assert indicator.valueAt(ticker.ticks.at(2 * sec)).equals(I.pair(3d, 3));
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).equals(I.pair(4d, 4));
        assert indicator.valueAt(ticker.ticks.at(4 * sec)).equals(I.pair(5d, 5));
    }

    @Test
    void map() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick).map(Tick::openPrice);
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
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> indicator = open.map(low, Num::plus);
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
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> high = Indicator.build(ticker, tick -> tick.highPrice);
        Indicator<Num> indicator = open.map(low, high, (o, l, h) -> o.plus(l).plus(h));
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
        DoubleIndicator indicator = Indicator.build(ticker, tick -> tick).dmap(t -> t.openPrice.doubleValue());
        assert indicator.valueAt(ticker.ticks.at(0 * sec)) == 1d;
        assert indicator.valueAt(ticker.ticks.at(1 * sec)) == 2d;
        assert indicator.valueAt(ticker.ticks.at(2 * sec)) == 3d;
        assert indicator.valueAt(ticker.ticks.at(3 * sec)) == 4d;
        assert indicator.valueAt(ticker.ticks.at(4 * sec)) == 5d;
    }

    @Test
    void mapToDoubleWith() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        long sec = ticker.span.seconds;
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        DoubleIndicator indicator = open.dmap(low, (o, l) -> o.plus(l).doubleValue());
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
        Indicator<Num> indicator = Indicator.build(ticker, tick -> {
            count.incrementAndGet();
            return tick.openPrice;
        });

        Indicator<Num> memo = indicator.memoize();
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
    void avoidMultiMemo() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> memoized = indicator.memoize();
        assert memoized != indicator;
        assert memoized == memoized.memoize();
    }

    @Test
    void dontCacheLatest() {
        Ticker ticker = ticker(Span.Minute5, 1, 2, 3);
        long sec = ticker.span.seconds;
        Tick tick2 = ticker.ticks.at(2 * sec);

        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.closePrice()).memoize();
        assert indicator.valueAt(tick2).is(3);

        // update latest price
        manager.update(Execution.with.buy(1).price(10));
        assert indicator.valueAt(tick2).is(10);

        // update latest price
        manager.update(Execution.with.buy(1).price(15));
        assert indicator.valueAt(tick2).is(15);

        // step into next tick
        manager.update(Execution.with.buy(1).price(20).date(tick2.openTime().plusSeconds(Span.Minute5.seconds)));
        assert indicator.valueAt(tick2).is(15);
        assert indicator.valueAt(ticker.ticks.at(3 * sec)).is(20);
    }
}