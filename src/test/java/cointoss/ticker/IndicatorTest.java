/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.I;
import kiss.Ⅱ;

class IndicatorTest extends TickerTestSupport {

    @Test
    void valueAt() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(5);
    }

    @Test
    void valueAtLowerTick() {
        Ticker ticker = ticker(TimeSpan.Second15, 1, 2, 3, 4, 5);
        Ticker lower = manager.of(TimeSpan.Second5);
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(lower.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(lower.ticks.getByIndex(1)).is(1);
        assert indicator.valueAt(lower.ticks.getByIndex(2)).is(1);
        assert indicator.valueAt(lower.ticks.getByIndex(3)).is(2);
        assert indicator.valueAt(lower.ticks.getByIndex(4)).is(2);
        assert indicator.valueAt(lower.ticks.getByIndex(5)).is(2);
        assert indicator.valueAt(lower.ticks.getByIndex(6)).is(3);
        assert indicator.valueAt(lower.ticks.getByIndex(12)).is(5);
    }

    @Test
    void combine() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Integer> open = Indicator.build(ticker, tick -> tick.openPrice.intValue());
        Indicator<Integer> low = Indicator.build(ticker, tick -> tick.lowPrice.intValue());
        Indicator<Ⅱ<Integer, Integer>> indicator = open.combine(low);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).equals(I.pair(1, 1));
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).equals(I.pair(2, 2));
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).equals(I.pair(3, 3));
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).equals(I.pair(4, 4));
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).equals(I.pair(5, 5));
    }

    @Test
    void map() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick).map(Tick::openPrice);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(5);
    }

    @Test
    void mapWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> indicator = open.map(low, Num::plus);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(6);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(8);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(10);
    }

    @Test
    void mapWith2() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> high = Indicator.build(ticker, tick -> tick.highPrice);
        Indicator<Num> indicator = open.map(low, high, (o, l, h) -> o.plus(l).plus(h));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(6);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(9);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(12);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(15);
    }

    @Test
    void mapToDouble() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = Indicator.build(ticker, tick -> tick).dmap(t -> t.openPrice.doubleValue());
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 5d;
    }

    @Test
    void mapToDoubleWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> open = Indicator.build(ticker, tick -> tick.openPrice);
        Indicator<Num> low = Indicator.build(ticker, tick -> tick.lowPrice);
        DoubleIndicator indicator = open.dmap(low, (o, l) -> o.plus(l).doubleValue());
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 6d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 8d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 10d;
    }

    @Test
    void memo() {
        AtomicInteger count = new AtomicInteger();

        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = Indicator.build(ticker, tick -> {
            count.incrementAndGet();
            return tick.openPrice;
        });

        Indicator<Num> memo = indicator.memoize();
        memo.valueAt(ticker.ticks.getByIndex(0));
        assert count.get() == 1;
        memo.valueAt(ticker.ticks.getByIndex(0));
        assert count.get() == 1;

        memo.valueAt(ticker.ticks.getByIndex(1));
        assert count.get() == 2;
        memo.valueAt(ticker.ticks.getByIndex(2));
        assert count.get() == 3;

        memo.valueAt(ticker.ticks.getByIndex(1));
        assert count.get() == 3;
        memo.valueAt(ticker.ticks.getByIndex(2));
        assert count.get() == 3;

        // from not memo
        indicator.valueAt(ticker.ticks.getByIndex(2));
        assert count.get() == 4;
        indicator.valueAt(ticker.ticks.getByIndex(2));
        assert count.get() == 5;
    }

    @Test
    void trueRange() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 4, 8, 16);
        NumIndicator indicator = Indicator.trueRange(ticker);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(0);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(8);
    }

    @Test
    void averageTrueRange() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 4, 8, 16);
        NumIndicator indicator = Indicator.averageTrueRange(ticker, 4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(0);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(0.25);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(0.6875);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(1.515625);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(3.13671875);
    }

    @Test
    void dontCacheLatest() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3);
        Tick tick2 = ticker.ticks.getByIndex(2);

        Indicator<Num> indicator = Indicator.build(ticker, tick -> tick.closePrice()).memoize();
        assert indicator.valueAt(tick2).is(3);

        // update latest price
        manager.update(Execution.with.buy(1).price(10));
        assert indicator.valueAt(tick2).is(10);

        // update latest price
        manager.update(Execution.with.buy(1).price(15));
        assert indicator.valueAt(tick2).is(15);

        // step into next tick
        manager.update(Execution.with.buy(1).price(20).date(tick2.end));
        assert indicator.valueAt(tick2).is(15);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(20);
    }
}
