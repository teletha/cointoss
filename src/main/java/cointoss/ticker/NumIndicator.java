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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cointoss.util.Num;
import kiss.I;

public abstract class NumIndicator extends IndicatableNumberBase<Num, NumIndicator> {

    /** The wrapped {@link NumIndicator}. (OPTIONAL: may be null) */
    protected final NumIndicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected NumIndicator(Function<Tick, Tick> normalizer) {
        super(normalizer);
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link NumIndicator}.
     * 
     * @param indicator A {@link NumIndicator} to delegate.
     */
    protected NumIndicator(NumIndicator indicator) {
        super(indicator.normalizer);
        this.wrapped = Objects.requireNonNull(indicator);
    }

    /**
     * Return the value of this {@link NumIndicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    @Override
    public Num valueAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
    }

    /**
     * Return the value of this {@link NumIndicator}. It is ensure that the {@link Tick} parameter
     * is rounded for {@link Ticker}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    protected abstract Num valueAtRounded(Tick tick);

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator scale(int scale) {
        return new NumIndicator(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return wrapped.valueAt(tick).scale(scale);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator ema(int size) {
        double multiplier = 2.0 / (size + 1);

        return memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            double prev = self.apply(tick.previous()).doubleValue();
            double now = valueAt(tick).doubleValue();

            return Num.of(((now - prev) * multiplier) + prev);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator mma(int size) {
        double multiplier = 1.0 / size;

        return memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            double prev = self.apply(tick.previous()).doubleValue();
            double now = valueAt(tick).doubleValue();

            return Num.of(((now - prev) * multiplier) + prev);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator sma(int size) {
        return new NumIndicator(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                double value = 0;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    value += wrapped.valueAt(current).doubleValue();
                    current = current.previous();
                    remaining--;
                }
                return Num.of(value / (size - remaining));
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator wma(int size) {
        return new NumIndicator(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                if (tick.previous() == null) {
                    return wrapped.valueAt(tick);
                }

                double value = 0;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value += wrapped.valueAt(tick).doubleValue() * i;
                    tick = tick.previous();
                }
                return Num.of(value / (actualSize * (actualSize + 1) / 2));
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final NumIndicator memoize(int limit, BiFunction<Tick, Function<Tick, Num>, Num> calculator) {
        return new NumIndicator(this) {

            /** CACHE */
            private final Cache<Tick, Num> cache = CacheBuilder.newBuilder().maximumSize(8192).weakKeys().weakValues().build();

            /** Call limit to avoid stack over flow. */
            private int count = limit;

            @Override
            protected Num valueAtRounded(Tick tick) {
                if (count == 0) return wrapped.valueAtRounded(tick);
                if (tick.closePrice == null /* The latest tick MUST NOT cache. */) return calculator.apply(tick, this::valueAt);

                try {
                    return cache.get(tick, () -> calculator.apply(tick, t -> {
                        count--;
                        Num v = this.valueAt(t);
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
     * Build your original {@link NumIndicator}.
     * 
     * @param <T>
     * @param ticker
     * @param calculator
     * @return
     */
    public static NumIndicator build(Ticker ticker, Function<Tick, Num> calculator) {
        Objects.requireNonNull(calculator);

        Function<Tick, Tick> normalizer = tick -> {
            Tick rounded = ticker.ticks.getByTime(tick.startSeconds);
            return rounded == null ? ticker.ticks.first() : rounded;
        };

        return new NumIndicator(normalizer) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return calculator.apply(tick);
            }
        };
    }
}
