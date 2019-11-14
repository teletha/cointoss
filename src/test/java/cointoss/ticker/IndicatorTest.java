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

import static cointoss.ticker.TickerTestSupport.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IndicatorTest {

    @Test
    void valueAt() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(2);
        assert indicator.valueAt(2).is(3);
        assert indicator.valueAt(3).is(4);
        assert indicator.valueAt(4).is(5);
    }

    @Test
    void valueAtOutOfRange() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice);

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> indicator.valueAt(-1));
        Assertions.assertThrows(NullPointerException.class, () -> indicator.valueAt(10));
    }

    @Test
    void first() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice);
        assert indicator.first().is(1);
    }

    @Test
    void last() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice);
        assert indicator.last().is(5);
    }

    @Test
    void sma() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice).sma(2);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.5);
        assert indicator.valueAt(2).is(2.5);
        assert indicator.valueAt(3).is(3.5);
        assert indicator.valueAt(4).is(4.5);

        indicator = Indicator.calculate(ticker, tick -> tick.openPrice).sma(4);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.5);
        assert indicator.valueAt(2).is(2);
        assert indicator.valueAt(3).is(2.5);
        assert indicator.valueAt(4).is(3.5);
    }

    @Test
    void ema() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice).ema(2);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.6666666667);
        assert indicator.valueAt(2).is(2.5555555556);
        assert indicator.valueAt(3).is(3.518518519);
        assert indicator.valueAt(4).is(4.50617284);

        indicator = Indicator.calculate(ticker, tick -> tick.openPrice).ema(4);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.4);
        assert indicator.valueAt(2).is(2.04);
        assert indicator.valueAt(3).is(2.824);
        assert indicator.valueAt(4).is(3.6944);
    }

    @Test
    void mma() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice).mma(2);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.5);
        assert indicator.valueAt(2).is(2.25);
        assert indicator.valueAt(3).is(3.125);
        assert indicator.valueAt(4).is(4.0625);

        indicator = Indicator.calculate(ticker, tick -> tick.openPrice).mma(4);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.25);
        assert indicator.valueAt(2).is(1.6875);
        assert indicator.valueAt(3).is(2.265625);
        assert indicator.valueAt(4).is(2.94921875);
    }

    @Test
    void wma() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 3, 4, 5);
        Indicator indicator = Indicator.calculate(ticker, tick -> tick.openPrice).wma(2);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.6666666667);
        assert indicator.valueAt(2).is(2.6666666667);
        assert indicator.valueAt(3).is(3.6666666667);
        assert indicator.valueAt(4).is(4.6666666667);

        indicator = Indicator.calculate(ticker, tick -> tick.openPrice).wma(4);
        assert indicator.valueAt(0).is(1);
        assert indicator.valueAt(1).is(1.6666666667);
        assert indicator.valueAt(2).is(2.333333333);
        assert indicator.valueAt(3).is(3);
        assert indicator.valueAt(4).is(4);
    }

    @Test
    void trueRange() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 4, 8, 16);
        Indicator indicator = Indicator.trueRange(ticker);
        assert indicator.valueAt(0).is(0);
        assert indicator.valueAt(1).is(1);
        assert indicator.valueAt(2).is(2);
        assert indicator.valueAt(3).is(4);
        assert indicator.valueAt(4).is(8);
    }

    @Test
    void averageTrueRange() {
        Ticker ticker = ticker(Span.Second5, 1, 2, 4, 8, 16);
        Indicator indicator = Indicator.averageTrueRange(ticker, 4);
        assert indicator.valueAt(0).is(0);
        assert indicator.valueAt(1).is(0.25);
        assert indicator.valueAt(2).is(0.6875);
        assert indicator.valueAt(3).is(1.515625);
        assert indicator.valueAt(4).is(3.13671875);
    }
}
