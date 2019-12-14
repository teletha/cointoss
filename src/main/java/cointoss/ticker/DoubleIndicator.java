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
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cointoss.util.Primitives;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;

public abstract class DoubleIndicator extends AbstractIndicator<Double> {

    /** The human-readable name. */
    public final Variable<String> name = Variable.of(getClass().getSimpleName());

    /** The mapper from timestamp to tick. */
    protected final Function<Tick, Tick> normalizer;

    /** The wrapped {@link DoubleIndicator}. (OPTIONAL: may be null) */
    protected final DoubleIndicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected DoubleIndicator() {
        this(Function.identity());
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected DoubleIndicator(Function<Tick, Tick> normalizer) {
        this.normalizer = normalizer;
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link DoubleIndicator}.
     * 
     * @param indicator A {@link DoubleIndicator} to delegate.
     */
    protected DoubleIndicator(DoubleIndicator indicator) {
        this.wrapped = Objects.requireNonNull(indicator);
        this.normalizer = Objects.requireNonNull(indicator.normalizer);
    }

    /**
     * Helper method to calculate the length of previous ticks.
     * 
     * @param tick A starting {@link Tick}.
     * @param max A maximum length you want.
     * @return The actual length of previous ticks.
     */
    protected final int calculatePreviousTickLength(Tick tick, int max) {
        int actualSize = 1;

        while (tick.previous() != null && actualSize < max) {
            tick = tick.previous();
            actualSize++;
        }
        return actualSize;
    }

    /**
     * Set the name property of this {@link DoubleIndicator}.
     * 
     * @param name The name value to set.
     */
    public final DoubleIndicator name(String name) {
        this.name.set(name);
        return this;
    }

    /**
     * Return the value of this {@link DoubleIndicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    public final double valueAt(Tick timestamp) {
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
     * Wrap by combined {@link DoubleIndicator}.
     * 
     * @param <With>
     * @param indicator1
     * @return
     */
    public final <With> Indicator<Ⅱ<Double, With>> combine(Indicator<With> indicator1) {
        return map(indicator1, (a, b) -> I.pair(a, b));
    }

    /**
     * Wrap by combined {@link DoubleIndicator}.
     * 
     * @param <With1>
     * @param <With2>
     * @param indicator1
     * @param indicator2
     * @return
     */
    public final <With1, With2> Indicator<Ⅲ<Double, With1, With2>> combine(Indicator<With1> indicator1, Indicator<With2> indicator2) {
        return map(indicator1, indicator2, (a, b, c) -> I.pair(a, b, c));
    }

    /**
     * Wrap by the mapped result.
     * 
     * @param <Out>
     * @param mapper
     * @return
     */
    public final <Out> Indicator<Out> map(DoubleFunction<Out> mapper) {
        return new Indicator<>() {
            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(DoubleIndicator.this.valueAt(tick));
            }
        };
    }

    /**
     * Wrap by the calculation result between {@link DoubleIndicator}s.
     * 
     * @param indicator1
     * @param calculater
     * @return
     */
    public final <With, Out> Indicator<Out> map(Indicator<With> indicator1, WiseBiFunction<Double, With, Out> calculater) {
        return new Indicator<Out>() {
            @Override
            protected Out valueAtRounded(Tick tick) {
                return calculater.apply(DoubleIndicator.this.valueAt(tick), indicator1.valueAt(tick));
            }
        };
    }

    /**
     * Wrap by the calculation result between {@link DoubleIndicator}s.
     * 
     * @param indicator1
     * @param indicator2
     * @param calculater
     * @return
     */
    public final <With1, With2, Out> Indicator<Out> map(Indicator<With1> indicator1, Indicator<With2> indicator2, WiseTriFunction<Double, With1, With2, Out> calculater) {
        return new Indicator<Out>() {

            @Override
            protected Out valueAtRounded(Tick tick) {
                return calculater.apply(DoubleIndicator.this.valueAt(tick), indicator1.valueAt(tick), indicator2.valueAt(tick));
            }
        };
    }

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
                return Primitives.roundDecimal(wrapped.valueAt(tick), scale);
            }
        };
    }

    /**
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
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
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator ema(Variable<? extends Number> size) {
        return ema(size.v.intValue());
    }

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
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
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator mma(Variable<? extends Number> size) {
        return mma(size.v.intValue());
    }

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator sma(int size) {
        return new DoubleIndicator(this) {

            @Override
            protected double valueAtRounded(Tick tick) {
                double value = 0;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    value += wrapped.valueAt(current);
                    current = current.previous();
                    remaining--;
                }
                return value / (size - remaining);
            }
        }.memoize();
    }

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator sma(Variable<? extends Number> size) {
        return sma(size.v.intValue());
    }

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator wma(int size) {
        return new DoubleIndicator(this) {

            @Override
            protected double valueAtRounded(Tick tick) {
                if (tick.previous() == null) {
                    return wrapped.valueAt(tick);
                }

                double value = 0;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value += wrapped.valueAt(tick) * i;
                    tick = tick.previous();
                }
                return value / (actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final DoubleIndicator wma(Variable<? extends Number> size) {
        return wma(size.v.intValue());
    }

    /**
     * Wrap by memoized {@link DoubleIndicator}.
     * 
     * @return
     */
    public final DoubleIndicator memoize() {
        return memoize(1, (tick, self) -> valueAt(tick));
    }

    /**
     * Wrap by memoized {@link DoubleIndicator} with the recursive caller.
     * 
     * @return
     */
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
                        double v = this.valueAt(t);
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
     * Create {@link Signal} of {@link DoubleIndicator} value.
     * 
     * @return
     */
    public final Signal<Double> observeWhen(Signal<Tick> timing) {
        return timing.map(this::valueAt);
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
