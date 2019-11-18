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

import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;

import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;

public abstract class Indicator<T> {

    /** The human-readable name. */
    public final Variable<String> name = Variable.of(getClass().getSimpleName());

    /** The target {@link Ticker}. */
    protected final Ticker ticker;

    /** The span length. */
    protected final long spanSeconds;

    /** The wrapped {@link Indicator}. (OPTIONAL: may be null) */
    protected final Indicator wrapped;

    /** The indicator property. */
    private boolean constant = false;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Ticker ticker) {
        this.ticker = Objects.requireNonNull(ticker);
        this.wrapped = null;
        this.spanSeconds = ticker.span.duration.toSeconds();
    }

    /**
     * Build with the delegation {@link Indicator}.
     * 
     * @param indicator A {@link Indicator} to delegate.
     */
    protected Indicator(Indicator indicator) {
        this.wrapped = Objects.requireNonNull(indicator);
        this.ticker = Objects.requireNonNull(indicator.ticker());
        this.spanSeconds = ticker.span.duration.toSeconds();
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

        while (tick.previous != null && actualSize < max) {
            tick = tick.previous;
            actualSize++;
        }
        return actualSize;
    }

    /**
     * Set the name property of this {@link Indicator}.
     * 
     * @param name The name value to set.
     */
    public final Indicator<T> name(String name) {
        this.name.set(name);
        return this;
    }

    /**
     * If this {@link Indicator} returns the constant value, this method will return true.
     * 
     * @return
     */
    public final boolean isConstant() {
        return constant;
    }

    /**
     * Return the related {@link Ticker}.
     * 
     * @return
     */
    public final Ticker ticker() {
        return ticker;
    }

    /**
     * Return the value of this {@link Indicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    public final T valueAt(Tick tick) {
        Tick rounded = ticker.findByEpochSecond(tick.startSeconds).v;
        return valueAtRounded(rounded == null ? ticker.first() : rounded);
    }

    /**
     * Return the value of this {@link Indicator}. It is ensure that the {@link Tick} parameter is
     * rounded for {@link Ticker}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    protected abstract T valueAtRounded(Tick tick);

    /**
     * Return the first value of this {@link Indicator}.
     * 
     * @return A first value.
     */
    public final T first() {
        return valueAt(ticker.first());
    }

    /**
     * Return the latest value of this {@link Indicator}.
     * 
     * @return A latest value.
     */
    public final T last() {
        return valueAt(ticker.last());
    }

    /**
     * Wrap by combined {@link Indicator}.
     * 
     * @param <With>
     * @param indicator1
     * @return
     */
    public final <With> Indicator<Ⅱ<T, With>> combine(Indicator<With> indicator1) {
        return map(indicator1, (a, b) -> I.pair(a, b));
    }

    /**
     * Wrap by combined {@link Indicator}.
     * 
     * @param <With1>
     * @param <With2>
     * @param indicator1
     * @param indicator2
     * @return
     */
    public final <With1, With2> Indicator<Ⅲ<T, With1, With2>> combine(Indicator<With1> indicator1, Indicator<With2> indicator2) {
        return map(indicator1, indicator2, (a, b, c) -> I.pair(a, b, c));
    }

    /**
     * Wrap by the mapped result.
     * 
     * @param <Out>
     * @param mapper
     * @return
     */
    public final <Out> Indicator<Out> map(Function<T, Out> mapper) {
        return (Indicator<Out>) memoize(1, (tick, self) -> (T) mapper.apply(valueAt(tick)));
    }

    /**
     * Wrap by the calculation result between {@link Indicator}s.
     * 
     * @param indicator1
     * @param calculater
     * @return
     */
    public final <With, Out> Indicator<Out> map(Indicator<With> indicator1, WiseBiFunction<T, With, Out> calculater) {
        return new Indicator<Out>(this) {

            @Override
            protected Out valueAtRounded(Tick tick) {
                return calculater.apply((T) wrapped.valueAtRounded(tick), indicator1.valueAt(tick));
            }
        };
    }

    /**
     * Wrap by the calculation result between {@link Indicator}s.
     * 
     * @param indicator1
     * @param indicator2
     * @param calculater
     * @return
     */
    public final <With1, With2, Out> Indicator<Out> map(Indicator<With1> indicator1, Indicator<With2> indicator2, WiseTriFunction<T, With1, With2, Out> calculater) {
        return new Indicator<>(this) {

            @Override
            protected Out valueAtRounded(Tick tick) {
                return calculater.apply((T) wrapped.valueAtRounded(tick), indicator1.valueAt(tick), indicator2.valueAt(tick));
            }
        };
    }

    /**
     * Wrap by exponetial moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> ema(int size) {
        Objects.checkIndex(size, 100);

        double multiplier = 2.0 / (size + 1);

        return (Indicator<Num>) memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous == null) {
                return valueAt(tick);
            }

            Num previous = (Num) self.apply(tick.previous);
            return (T) ((Num) valueAt(tick)).minus(previous).multiply(multiplier).plus(previous);
        });
    }

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> mma(int size) {
        Objects.checkIndex(size, 100);

        double multiplier = 1.0 / size;

        return (Indicator<Num>) memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous == null) {
                return valueAt(tick);
            }

            Num previous = (Num) self.apply(tick.previous);
            return (T) ((Num) valueAt(tick)).minus(previous).multiply(multiplier).plus(previous);
        });
    }

    /**
     * Wrap by simple moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> sma(int size) {
        Objects.checkIndex(size, 256);

        return new Indicator<Num>(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                Num sum = Num.ZERO;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    sum = sum.plus((Num) wrapped.valueAt(current));
                    current = current.previous;
                    remaining--;
                }
                return sum.divide(size - remaining);
            }
        }.memoize();
    }

    /**
     * Wrap by weighted moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> wma(int size) {
        Objects.checkIndex(size, 100);

        return new Indicator<Num>(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                if (tick.previous == null) {
                    return (Num) wrapped.valueAt(tick);
                }

                Num value = Num.ZERO;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value = value.plus(((Num) wrapped.valueAt(tick)).multiply(i));
                    tick = tick.previous;
                }
                return value.divide(actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * Wrap by memoized {@link Indicator}.
     * 
     * @return
     */
    public final Indicator<T> memoize() {
        return memoize(1, (tick, self) -> valueAt(tick));
    }

    /**
     * Wrap by memoized {@link Indicator} with the recursive caller.
     * 
     * @return
     */
    public final Indicator<T> memoize(int limit, BiFunction<Tick, Function<Tick, T>, T> calculator) {
        return new Indicator<T>(this) {

            /** CACHE */
            private final MutableLongObjectMap<T> cache = LongObjectMaps.mutable.empty();

            /** Call limit to avoid stack over flow. */
            private int count = limit;

            @Override
            protected T valueAtRounded(Tick tick) {
                if (count == 0) return (T) wrapped.valueAt(tick);

                return cache.getIfAbsentPut(tick.startSeconds, () -> calculator.apply(tick, t -> {
                    count--;
                    T v = this.valueAt(t);
                    count++;
                    return v;
                }));
            }
        };
    }

    /**
     * Create {@link Signal} of {@link Indicator} value.
     * 
     * @return
     */
    public final Signal<T> observe() {
        return ticker.add.map(this::valueAt);
    }

    /**
     * Create {@link Signal} of {@link Indicator} value.
     * 
     * @return
     */
    public final Signal<T> observeNow() {
        return observe().startWith(last());
    }

    /**
     * Build constant {@link Indicator}.
     * 
     * @param <T>
     * @param ticker
     * @param value The constant value.
     * @return
     */
    public static <T> Indicator<T> build(Ticker ticker, T value) {
        Objects.requireNonNull(value);

        Indicator<T> indicator = new Indicator<>(ticker) {

            @Override
            protected T valueAtRounded(Tick tick) {
                return value;
            }
        };
        indicator.constant = true;
        return indicator;
    }

    /**
     * Build your original {@link Indicator}.
     * 
     * @param <T>
     * @param ticker
     * @param calculator
     * @return
     */
    public static <T> Indicator<T> build(Ticker ticker, Function<Tick, T> calculator) {
        Objects.requireNonNull(calculator);

        return new Indicator<>(ticker) {

            @Override
            protected T valueAtRounded(Tick tick) {
                return calculator.apply(tick);
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
    public static Indicator<Num> averageTrueRange(Ticker ticker, int size) {
        return trueRange(ticker).mma(size);
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
    public static Indicator<Num> trueRange(Ticker ticker) {
        return new Indicator<>(ticker) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                Num highLow = tick.highPrice().minus(tick.lowPrice()).abs();

                if (tick.previous == null) {
                    return highLow;
                }

                Tick previous = tick.previous;
                Num highClose = tick.highPrice().minus(previous.closePrice()).abs();
                Num closeLow = previous.closePrice().minus(tick.lowPrice).abs();

                return Num.max(highLow, highClose, closeLow);
            }
        };
    }
}
