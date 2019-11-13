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
     * Wrap by exponetial moving average indicator.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator ema(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(ticker()) {

            /** The multiplier. */
            private final double multiplier = 2.0 / (size + 1);

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return Indicator.this.valueAt(0);
                }

                Num prevValue = valueAt(index - 1);
                return Indicator.this.valueAt(index).minus(prevValue).multiply(multiplier).plus(prevValue);
            }
        };
    }

    /**
     * Wrap by simple moving average indicator.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator sma(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(ticker()) {

            @Override
            protected Num calculate(int index) {
                Num sum = Num.ZERO;
                for (int i = Math.max(0, index - size + 1); i <= index; i++) {
                    sum = sum.plus(Indicator.this.valueAt(i));
                }
                return sum.divide(Math.min(size, index + 1));
            }
        };
    }

    /**
     * Wrap by weighted moving average indicator.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    default Indicator wma(int size) {
        Objects.checkIndex(size, 100);

        return new AbstractCachedIndicator(ticker()) {

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return Indicator.this.valueAt(index);
                }

                Num value = Num.ZERO;

                if (index - size < 0) {
                    for (int i = index + 1; i > 0; i--) {
                        value = value.plus(Num.of(i).multiply(Indicator.this.valueAt(i - 1)));
                    }
                    return value.divide(Num.of(((index + 1) * (index + 2)) / 2));
                } else {
                    int actualIndex = index;
                    for (int i = size; i > 0; i--) {
                        value = value.plus(Num.of(i).multiply(Indicator.this.valueAt(actualIndex)));
                        actualIndex--;
                    }
                    return value.divide(Num.of((size * (size + 1)) / 2));
                }
            }
        };
    }

    /**
     * Build simple calculatable {@link Indicator}.
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
}
