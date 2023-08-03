/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import cointoss.util.arithmetic.Primitives;

public abstract class DoubleIndicator extends AbstractNumberIndicator<Double, DoubleIndicator> {

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected DoubleIndicator(Ticker ticker, Function<Tick, Tick> normalizer) {
        super(ticker, normalizer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double valueAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
    }

    /**
     * Specialized method.
     * 
     * @param timestamp
     * @return
     */
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
        return new DoubleIndicator(ticker, normalizer) {

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
    public final DoubleIndicator scale(int size) {
        return new DoubleIndicator(ticker, normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return Primitives.roundDecimal(DoubleIndicator.this.doubleAt(tick), size);
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
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return valueAt(tick);
            }

            double prev = self.apply(before);
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
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return valueAt(tick);
            }

            double prev = self.apply(before);
            double now = valueAt(tick);

            return ((now - prev) * multiplier) + prev;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator sma(int size) {
        return new DoubleIndicator(ticker, normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                double value = 0;
                List<Tick> before = ticker.ticks.query(tick, o -> o.reverse().max(size)).toList();
                int actualSize = before.size();
                for (int i = 0; i < actualSize; i++) {
                    value += DoubleIndicator.this.doubleAt(before.get(i));
                }
                return value / actualSize;
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator wma(int size) {
        return new DoubleIndicator(ticker, normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                double value = 0;
                List<Tick> previous = ticker.ticks.query(tick, o -> o.reverse().max(size)).toList();
                int actualSize = previous.size();
                for (int i = 0; i < actualSize; i++) {
                    value += DoubleIndicator.this.doubleAt(previous.get(i)) * (actualSize - i);
                }
                return value / (actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * Build your original {@link DoubleIndicator}.
     * 
     * @param ticker
     * @param calculator
     * @return
     */
    public static DoubleIndicator build(Ticker ticker, ToDoubleFunction<Tick> calculator) {
        Objects.requireNonNull(calculator);

        Function<Tick, Tick> normalizer = tick -> {
            Tick rounded = ticker.ticks.at(tick.openTime);
            return rounded == null ? ticker.ticks.first() : rounded;
        };

        return new DoubleIndicator(ticker, normalizer) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return calculator.applyAsDouble(tick);
            }
        };
    }
}