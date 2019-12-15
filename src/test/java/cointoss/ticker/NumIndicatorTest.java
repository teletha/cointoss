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

class NumIndicatorTest extends TickerTestSupport {

    @Test
    void valueAt() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice);
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
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice);
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
        NumIndicator open = NumIndicator.build(ticker, tick -> tick.openPrice);
        NumIndicator low = NumIndicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Ⅱ<Num, Num>> indicator = open.combine(low);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).equals(I.pair(Num.of(1), Num.of(1)));
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).equals(I.pair(Num.of(2), Num.of(2)));
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).equals(I.pair(Num.of(3), Num.of(3)));
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).equals(I.pair(Num.of(4), Num.of(4)));
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).equals(I.pair(Num.of(5), Num.of(5)));
    }

    @Test
    void map() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        Indicator<Num> indicator = NumIndicator.build(ticker, tick -> tick.openPrice).map(v -> v.plus(1));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(5);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(6);
    }

    @Test
    void mapWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator open = NumIndicator.build(ticker, tick -> tick.openPrice);
        NumIndicator low = NumIndicator.build(ticker, tick -> tick.lowPrice);
        Indicator<Num> indicator = open.map(low, (o, l) -> o.plus(l));
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(6);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(8);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(10);
    }

    @Test
    void mapWith2() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator open = NumIndicator.build(ticker, tick -> tick.openPrice);
        NumIndicator low = NumIndicator.build(ticker, tick -> tick.lowPrice);
        NumIndicator high = NumIndicator.build(ticker, tick -> tick.highPrice);
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
        DoubleIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).dmap(v -> v.doubleValue() + 1);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)) == 2d;
        assert indicator.valueAt(ticker.ticks.getByIndex(1)) == 3d;
        assert indicator.valueAt(ticker.ticks.getByIndex(2)) == 4d;
        assert indicator.valueAt(ticker.ticks.getByIndex(3)) == 5d;
        assert indicator.valueAt(ticker.ticks.getByIndex(4)) == 6d;
    }

    @Test
    void mapToDoubleWith() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator open = NumIndicator.build(ticker, tick -> tick.openPrice);
        NumIndicator low = NumIndicator.build(ticker, tick -> tick.lowPrice);
        DoubleIndicator indicator = open.dmap(low, (o, l) -> o.doubleValue() + l.doubleValue());
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
        NumIndicator indicator = NumIndicator.build(ticker, tick -> {
            count.incrementAndGet();
            return tick.openPrice;
        });

        NumIndicator memo = indicator.memoize();
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
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).scale(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1.2);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(2.3);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(3.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4.6);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(5.7);
    }

    @Test
    void sma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).sma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(3.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(4.5);

        indicator = NumIndicator.build(ticker, tick -> tick.openPrice).sma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(2.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(3.5);
    }

    @Test
    void ema() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).ema(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.6666666666666665);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.5555555555555554);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(3.518518518518518);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(4.506172839506172);

        indicator = NumIndicator.build(ticker, tick -> tick.openPrice).ema(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.4);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.04);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(2.824);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(3.6944);
    }

    @Test
    void mma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).mma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.5);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.25);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(3.125);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(4.0625);

        indicator = NumIndicator.build(ticker, tick -> tick.openPrice).mma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.25);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(1.6875);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(2.265625);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(2.94921875);
    }

    @Test
    void wma() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3, 4, 5);
        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.openPrice).wma(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.6666666666666667);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.6666666666666667);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(3.6666666666666667);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(4.6666666666666667);

        indicator = NumIndicator.build(ticker, tick -> tick.openPrice).wma(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1.6666666666666667);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2.3333333333333333);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(3);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(4);
    }

    @Test
    void dontCacheLatest() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 3);
        Tick tick2 = ticker.ticks.getByIndex(2);

        NumIndicator indicator = NumIndicator.build(ticker, tick -> tick.closePrice()).memoize();
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

    @Test
    void trueRange() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 4, 8, 16);
        NumIndicator indicator = NumIndicator.trueRange(ticker);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(0);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(1);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(2);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(4);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(8);
    }

    @Test
    void averageTrueRange() {
        Ticker ticker = ticker(TimeSpan.Second5, 1, 2, 4, 8, 16);
        NumIndicator indicator = NumIndicator.averageTrueRange(ticker, 4);
        assert indicator.valueAt(ticker.ticks.getByIndex(0)).is(0);
        assert indicator.valueAt(ticker.ticks.getByIndex(1)).is(0.25);
        assert indicator.valueAt(ticker.ticks.getByIndex(2)).is(0.6875);
        assert indicator.valueAt(ticker.ticks.getByIndex(3)).is(1.515625);
        assert indicator.valueAt(ticker.ticks.getByIndex(4)).is(3.13671875);
    }
}