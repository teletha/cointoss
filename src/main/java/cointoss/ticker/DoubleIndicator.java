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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import cointoss.util.Primitives;

public abstract class DoubleIndicator extends IndicatableNumberBase<Double, DoubleIndicator> {

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected DoubleIndicator(Function<Tick, Tick> normalizer) {
        super(normalizer);
    }

    /**
     * Return the value of this {@link DoubleIndicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    @Override
    public Double valueAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
    }

    protected final double doubleAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
    }

    /**
     * Return the value of this {@link DoubleIndicator}. It is ensure that the {@link Tick}
     * parameter is rounded for {@link Ticker}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    protected abstract double valueAtRounded(Tick tick);

    /**
     * {@inheritDoc}
     */
    @Override
    protected DoubleIndicator build(BiFunction<Tick, DoubleIndicator, Double> delegator) {
        return new DoubleIndicator(normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return delegator.apply(tick, this);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator scale(int scale) {
        return new DoubleIndicator(normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return Primitives.roundDecimal(DoubleIndicator.this.doubleAt(tick), scale);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator ema(int size) {
        double multiplier = 2.0 / (size + 1);

        return memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            double prev = self.apply(tick.previous());
            double now = valueAt(tick);

            return ((now - prev) * multiplier) + prev;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator mma(int size) {
        double multiplier = 1.0 / size;

        return memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            double prev = self.apply(tick.previous());
            double now = valueAt(tick);

            return ((now - prev) * multiplier) + prev;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator sma(int size) {
        return new DoubleIndicator(normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                double value = 0;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    value += DoubleIndicator.this.doubleAt(current);
                    current = current.previous();
                    remaining--;
                }
                return value / (size - remaining);
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator wma(int size) {
        return new DoubleIndicator(normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                if (tick.previous() == null) {
                    return DoubleIndicator.this.doubleAt(tick);
                }

                double value = 0;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value += DoubleIndicator.this.doubleAt(tick) * i;
                    tick = tick.previous();
                }
                return value / (actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * Build your original {@link DoubleIndicator}.
     * 
     * @param <T>
     * @param ticker
     * @param calculator
     * @return
     */
    public static DoubleIndicator build(Ticker ticker, ToDoubleFunction<Tick> calculator) {
        Objects.requireNonNull(calculator);

        Function<Tick, Tick> normalizer = tick -> {
            Tick rounded = ticker.ticks.getByTime(tick.startSeconds);
            return rounded == null ? ticker.ticks.first() : rounded;
        };

        return new DoubleIndicator(normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return calculator.applyAsDouble(tick);
            }
        };
    }
}
