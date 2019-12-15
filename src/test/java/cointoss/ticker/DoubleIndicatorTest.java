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

class DoubleIndicatorTest extends TickerTestSupport {

    @Test
    void valueAt() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue());
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 5d;
    }

    @Test
    void valueAtLowerTick() {
        Ticker ticker = ticker(TimeSpan.Second15, 1, 2, 3, 4, 5);
        Ticker lower = manager.of(TimeSpan.Second5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue());
        assert indicator.valueAt(lower.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(lower.ticks.getByIndex(1)) == 1d;
        assert indicator.valueAt(lower.ticks.getByIndex(2)) == 1d;
        assert indicator.valueAt(lower.ticks.getByIndex(3)) == 2d;
        assert indicator.valueAt(lower.ticks.getByIndex(4)) == 2d;
        assert indicator.valueAt(lower.ticks.getByIndex(5)) == 2d;
        assert indicator.valueAt(lower.ticks.getByIndex(6)) == 3d;
        assert indicator.valueAt(lower.ticks.getByIndex(12)) == 5d;
    }

    @Test
    void map() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).map(v -> Num.of(v));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(5);
    }

    @Test
    void mapWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue());
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice.doubleValue());
        Indicatable<Num> indicator = open.map(low, (o, l) -> Num.of(o).plus(l));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(6);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(8);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(10);
    }

    @Test
    void mapWith2() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue());
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice.doubleValue());
        DoubleIndicator high = DoubleIndicator.build(ticker, tick -> tick.highPrice.doubleValue());
        Indicatable<Num> indicator = open.map(low, high, (o, l, h) -> Num.of(o).plus(l).plus(h));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(6);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(9);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(12);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(15);
    }

    @Test
    void mapToDouble() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).dmap(v -> v + 1);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 6d;
    }

    @Test
    void mapToDoubleWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator open = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue());
        DoubleIndicator low = DoubleIndicator.build(ticker, tick -> tick.lowPrice.doubleValue());
        DoubleIndicator indicator = open.dmap(low, (o, l) -> o + l);
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
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> {
            count.incrementAndGet();
            return tick.openPrice.doubleValue();
        });

        DoubleIndicator memo = indicator.memoize();
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
    void scale() {
        Ticker ticker = ticker(TimeSpan.Second5, 1.23, 2.34, 3.45, 4.56, 5.67);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).scale(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1.2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 2.3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 3.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 4.6d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 5.7d;
    }

    @Test
    void sma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).sma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 3.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 4.5d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).sma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 2.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 3.5d;
    }

    @Test
    void ema() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).ema(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.6666666666666665;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.5555555555555554d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 3.518518518518518d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 4.506172839506172d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).ema(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.04d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 2.824d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 3.6944d;
    }

    @Test
    void mma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).mma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.25d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 3.125d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 4.0625d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).mma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.25d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 1.6875d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 2.265625d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 2.94921875d;
    }

    @Test
    void wma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).wma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 3.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 4.6666666666666667d;

        indicator = DoubleIndicator.build(ticker, tick -> tick.openPrice.doubleValue()).wma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 1d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 1.6666666666666667d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 2.3333333333333333d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 4d;
    }

    @Test
    void dontCacheLatest() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3);
        Tick tick2 = ticker.ticks.getByIndex(2);

        DoubleIndicator indicator = DoubleIndicator.build(ticker, tick -> tick.closePrice().doubleValue()).memoize();
        assert indicator.valueAt(tick2) == 3d;

        // update latest price
        manager.update(Execution.with.buy(1).price(10));
        assert indicator.valueAt(tick2) == 10d;

        // update latest price
        manager.update(Execution.with.buy(1).price(15));
        assert indicator.valueAt(tick2) == 15d;

        // step into next tick
        manager.update(Execution.with.buy(1).price(20).date(tick2.end));
        assert indicator.valueAt(tick2) == 15d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 20d;
    }
}
