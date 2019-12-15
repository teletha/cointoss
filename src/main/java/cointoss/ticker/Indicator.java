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
import kiss.Variable;

public abstract class Indicator<T> extends Indicatable<T, Indicator<T>> {

    /** The wrapped {@link Indicator}. (OPTIONAL: may be null) */
    protected final Indicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Function<Tick, Tick> normalizer) {
        super(normalizer);
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link Indicator}.
     * 
     * @param indicator A {@link Indicator} to delegate.
     */
    protected Indicator(Indicator indicator) {
        super(indicator.normalizer);
        this.wrapped = Objects.requireNonNull(indicator);
    }

    /**
     * Return the value of this {@link Indicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    @Override
    public T valueAt(Tick timestamp) {
        return valueAtRounded(normalizer.apply(timestamp));
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
     * Wrap by scaled value.
     * 
     * @param scale Scale size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> scale(int scale) {
        return new Indicator<Num>(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                return ((Num) wrapped.valueAt(tick)).scale(scale);
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
        double multiplier = 2.0 / (size + 1);

        return (Indicator<Num>) memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            double previous = ((Num) self.apply(tick.previous())).doubleValue();
            Num now = (Num) valueAt(tick);

            return (T) Num.of(((now.doubleValue() - previous) * multiplier) + previous);
        });
    }

    /**
     * Wrap by modified moving average.
     * 
     * @param size A tick size.
     * @return A wrapped indicator.
     */
    public final Indicator<Num> mma(int size) {
        double multiplier = 1.0 / size;

        return (Indicator<Num>) memoize((size + 1) * 4, (tick, self) -> {
            if (tick.previous() == null) {
                return valueAt(tick);
            }

            Num previous = (Num) self.apply(tick.previous());
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
        return new Indicator<Num>(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                double value = 0;
                Tick current = tick;
                int remaining = size;
                while (current != null && 0 < remaining) {
                    Num num = (Num) wrapped.valueAt(current);
                    value += num.doubleValue();
                    current = current.previous();
                    remaining--;
                }
                return Num.of(value / (size - remaining));
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
        return new Indicator<Num>(this) {

            @Override
            protected Num valueAtRounded(Tick tick) {
                if (tick.previous() == null) {
                    return (Num) wrapped.valueAt(tick);
                }

                Num value = Num.ZERO;
                int actualSize = calculatePreviousTickLength(tick, size);
                for (int i = actualSize; 0 < i; i--) {
                    value = value.plus(((Num) wrapped.valueAt(tick)).multiply(i));
                    tick = tick.previous();
                }
                return value.divide(actualSize * (actualSize + 1) / 2);
            }
        }.memoize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Indicator<T> memoize(int limit, BiFunction<Tick, Function<Tick, T>, T> calculator) {
        return new Indicator<T>(this) {

            /** CACHE */
            private final Cache<Tick, T> cache = CacheBuilder.newBuilder().maximumSize(8192).weakKeys().weakValues().build();

            /** Call limit to avoid stack over flow. */
            private int count = limit;

            @Override
            protected T valueAtRounded(Tick tick) {
                if (count == 0) return (T) wrapped.valueAtRounded(tick);
                if (tick.closePrice == null /* The latest tick MUST NOT cache. */) return calculator.apply(tick, this::valueAt);

                try {
                    return cache.get(tick, () -> calculator.apply(tick, t -> {
                        count--;
                        T v = this.valueAt(t);
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
     * Build your original {@link Indicator}.
     * 
     * @param <T>
     * @param ticker
     * @param calculator
     * @return
     */
    public static <T> Indicator<T> build(Ticker ticker, Function<Tick, T> calculator) {
        Objects.requireNonNull(calculator);

        Function<Tick, Tick> normalizer = tick -> {
            Tick rounded = ticker.ticks.getByTime(tick.startSeconds);
            return rounded == null ? ticker.ticks.first() : rounded;
        };

        return new Indicator<>(normalizer) {

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
        return build(ticker, tick -> {
            Num highLow = tick.highPrice().minus(tick.lowPrice()).abs();

            if (tick.previous() == null) {
                return highLow;
            }

            Tick previous = tick.previous();
            Num highClose = tick.highPrice().minus(previous.closePrice()).abs();
            Num closeLow = previous.closePrice().minus(tick.lowPrice).abs();

            return Num.max(highLow, highClose, closeLow);
        });
    }
}
