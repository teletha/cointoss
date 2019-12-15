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
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cointoss.util.Primitives;
import kiss.I;

public abstract class DoubleIndicator extends IndicatableNumberBase<Double, DoubleIndicator> {

    /** The wrapped {@link DoubleIndicator}. (OPTIONAL: may be null) */
    protected final DoubleIndicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected DoubleIndicator(Function<Tick, Tick> normalizer) {
        super(normalizer);
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link DoubleIndicator}.
     * 
     * @param indicator A {@link DoubleIndicator} to delegate.
     */
    protected DoubleIndicator(DoubleIndicator indicator) {
        super(indicator.normalizer);
        this.wrapped = Objects.requireNonNull(indicator);
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
     * Wrap by scaled value.
     * 
     * @param scale Scale size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator scale(int scale) {
        return new DoubleIndicator(this) {

            @Override
            protected double valueAtRounded(Tick tick) {
                return Primitives.roundDecimal(wrapped.doubleAt(tick), scale);
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
        return new DoubleIndicator(this) {

            @Override
            protected double valueAtRounded(Tick tick) {
                double value = 0;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    value += wrapped.doubleAt(current);
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
        return new DoubleIndicator(this) {

            @Override
            protected double valueAtRounded(Tick tick) {
                if (tick.previous() == null) {
                    return wrapped.doubleAt(tick);
                }

                double value = 0;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value += wrapped.doubleAt(tick) * i;
                    tick = tick.previous();
                }
                return value / (actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DoubleIndicator memoize(int limit, BiFunction<Tick, Function<Tick, Double>, Double> calculator) {
        return new DoubleIndicator(this) {

            /** CACHE */
            private final Cache<Tick, Double> cache = CacheBuilder.newBuilder().maximumSize(8192).weakKeys().weakValues().build();

            /** Call limit to avoid stack over flow. */
            private int count = limit;

            @Override
            protected double valueAtRounded(Tick tick) {
                if (count == 0) return wrapped.valueAtRounded(tick);
                if (tick.closePrice == null /* The latest tick MUST NOT cache. */) return calculator.apply(tick, this::valueAt);

                try {
                    return cache.get(tick, () -> calculator.apply(tick, t -> {
                        count--;
                        double v = this.doubleAt(t);
                        count++;
                        return v;
                    }));
                } catch (ExecutionException e) {
                    throw I.quiet(e);
                }
            }
        };

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
