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

import java.util.Objects;
import java.util.function.Function;

import cointoss.util.Num;

public interface Indicator {

    /**
     * Return the related {@link Ticker}.
     * 
     * @return
     */
    Ticker ticker();

    /**
     * Return the value of this {@link Indicator}.
     * 
     * @param index A index on {@link Ticker}.
     * @return
     */
    Num valueAt(int index);

    /**
     * Return the first value of this {@link Indicator}.
     * 
     * @return A first value.
     */
    default Num first() {
        return valueAt(0);
    }

    /**
     * Return the latest value of this {@link Indicator}.
     * 
     * @return A latest value.
     */
    default Num last() {
        return valueAt(Math.max(0, ticker().size() - 1));
    }

    /**
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator ema(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(this) {

            /** The multiplier. */
            private final double multiplier = 2.0 / (size + 1);

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return wrapped.valueAt(0);
                }

                Num previous = valueAt(index - 1);
                return wrapped.valueAt(index).minus(previous).multiply(multiplier).plus(previous);
            }
        };
    }

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator mma(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(this) {

            /** The multiplier. */
            private final double multiplier = 1.0 / size;

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return wrapped.valueAt(0);
                }

                Num previous = valueAt(index - 1);
                return wrapped.valueAt(index).minus(previous).multiply(multiplier).plus(previous);
            }
        };
    }

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator sma(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(this) {

            @Override
            protected Num calculate(int index) {
                Num sum = Num.ZERO;
                for (int i = Math.max(0, index - size + 1); i <= index; i++) {
                    sum = sum.plus(wrapped.valueAt(i));
                }
                return sum.divide(Math.min(size, index + 1));
            }
        };
    }

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator wma(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(this) {

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return wrapped.valueAt(index);
                }

                Num value = Num.ZERO;

                if (index - size < 0) {
                    for (int i = index + 1; i > 0; i--) {
                        value = value.plus(Num.of(i).multiply(wrapped.valueAt(i - 1)));
                    }
                    return value.divide(Num.of(((index + 1) * (index + 2)) / 2));
                } else {
                    int actualIndex = index;
                    for (int i = size; i > 0; i--) {
                        value = value.plus(Num.of(i).multiply(wrapped.valueAt(actualIndex)));
                        actualIndex--;
                    }
                    return value.divide(Num.of((size * (size + 1)) / 2));
                }
            }
        };
    }

    /**
     * Build your original {@link Indicator}.
     * 
     * @param <T>
     * @param ticker
     * @param calculator
     * @return
     */
    static Indicator calculate(Ticker ticker, Function<Tick, Num> calculator) {
        Objects.requireNonNull(calculator);

        return new AbstractCachedIndicator(ticker) {

            @Override
            protected Num calculate(int index) {
                return calculator.apply(ticker.get(index));
            }
        };
    }

    /**
     * <p>
     * Welles Wilder described these calculations to determine the trading range for a stock or
     * commodity. True Range is defined as the largest of the following:
     * </p>
     * <ul>
     * <li>The distance from today's high to today's low.</li>
     * <li>The distance from yesterday's close to today's high.</li>
     * <li>The distance from yesterday's close to today's low.</li>
     * </ul>
     * <p>
     * Wilder included price comparisons among subsequent bars in order to account for gaps in his
     * range calculation.
     * <p>
     * <p>
     * The raw True Range is then smoothed (a 14-period smoothing is common) to give an Average True
     * Range (ATR). The True Range can be smoothed using a variety of moving average types,
     * including Simple, Exponential, Welles Wilder, etc.
     * </p>
     * <p>
     * ATR measures a security's volatility. It does not indicate price direction or duration,
     * rather the degree of price movement. Average True Range can be interpreted using the same
     * techniques that are used with the other volatility indicators.
     * </p>
     * <p>
     * Wilder states that high values of ATR often occur at market bottoms following a sell-off. Low
     * ATR values are often found during extended sideways or consolidation periods.
     * </p>
     * <p>
     * Several other indicators are built off True Range, including DI+/DI-, ADX, and ADXR. RTL
     * Tokens . . . TR ( more )
     * </p>
     * 
     * @param ticker
     * @return
     */
    static Indicator trueRange(Ticker ticker) {
        return new AbstractCachedIndicator(ticker) {

            @Override
            protected Num calculate(int index) {
                Tick current = ticker.get(index);
                Num highLow = current.highPrice().minus(current.lowPrice()).abs();

                if (index == 0) {
                    return highLow;
                }

                Tick previous = ticker.get(index - 1);
                Num highClose = index == 0 ? Num.ZERO : current.highPrice().minus(previous.closePrice()).abs();
                Num closeLow = index == 0 ? Num.ZERO : previous.closePrice().minus(current.lowPrice).abs();

                return Num.max(highLow, highClose, closeLow);
            }
        };
    }

    /**
     * <p>
     * The average true range (ATR) is a technical analysis indicator that measures market
     * volatility by decomposing the entire range of an asset price for that period. Specifically,
     * ATR is a measure of volatility introduced by market technician J. Welles Wilder Jr. in his
     * book, "New Concepts in Technical Trading Systems."
     * </p>
     * <p>
     * The true range indicator is taken as the greatest of the following: current high less the
     * current low; the absolute value of the current high less the previous close; and the absolute
     * value of the current low less the previous close. The average true range is then a moving
     * average, generally using 14 days, of the true ranges.
     * </p>
     * 
     * @param ticker
     * @param size
     * @return
     */
    static Indicator averageTrueRange(Ticker ticker, int size) {
        return trueRange(ticker).mma(size);
    }
}
