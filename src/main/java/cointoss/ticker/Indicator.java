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

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    /** The wrapped {@link Indicator}. (OPTIONAL: may be null) */
    protected final Indicator wrapped;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Ticker ticker) {
        this.ticker = Objects.requireNonNull(ticker);
        this.wrapped = null;
    }

    /**
     * Build with the delegation {@link Indicator}.
     * 
     * @param indicator A {@link Indicator} to delegate.
     */
    protected Indicator(Indicator indicator) {
        this.wrapped = Objects.requireNonNull(indicator);
        this.ticker = Objects.requireNonNull(indicator.ticker());
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
     * @return
     */
    public abstract T valueAt(Tick tick);

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
     * Wrap by the calculation result between {@link Indicator}s.
     * 
     * @param indicator1
     * @param calculater
     * @return
     */
    public final <With, Out> Indicator<Out> calculate(Indicator<With> indicator1, WiseBiFunction<T, With, Out> calculater) {
        return new Indicator<Out>(this) {

            @Override
            public Out valueAt(Tick tick) {
                return calculater.apply((T) wrapped.valueAt(tick), indicator1.valueAt(tick));
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
    public final <With1, With2, Out> Indicator<Out> calculate(Indicator<With1> indicator1, Indicator<With2> indicator2, WiseTriFunction<T, With1, With2, Out> calculater) {
        return new Indicator<>(this) {

            @Override
            public Out valueAt(Tick tick) {
                return calculater.apply((T) wrapped.valueAt(tick), indicator1.valueAt(tick), indicator2.valueAt(tick));
            }
        };
    }

    /**
     * Wrap by combined {@link Indicator}.
     * 
     * @param <With>
     * @param indicator1
     * @return
     */
    public final <With> Indicator<Ⅱ<T, With>> combine(Indicator<With> indicator1) {
        return calculate(indicator1, (a, b) -> I.pair(a, b));
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
        return calculate(indicator1, indicator2, (a, b, c) -> I.pair(a, b, c));
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

        return (Indicator<Num>) memoize((tick, self) -> {
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

        return (Indicator<Num>) memoize((tick, self) -> {
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
        Objects.checkIndex(size, 100);

        return new Indicator<Num>(this) {

            @Override
            public Num valueAt(Tick tick) {
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
            public Num valueAt(Tick tick) {
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
        return memoize((tick, self) -> valueAt(tick));
    }

    /**
     * Wrap by memoized {@link Indicator} with the recursive caller.
     * 
     * @return
     */
    public final Indicator<T> memoize(BiFunction<Tick, Function<Tick, T>, T> calculator) {
        return new Indicator<>(this) {

            /** CACHE */
            private final ConcurrentSkipListMap<ZonedDateTime, T> cache = new ConcurrentSkipListMap();

            @Override
            public T valueAt(Tick tick) {
                return cache.computeIfAbsent(tick.start, key -> calculator.apply(tick, this::valueAt));
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
            public T valueAt(Tick tick) {
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
            public Num valueAt(Tick tick) {
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
