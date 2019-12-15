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

public abstract class Indicator<T> extends Indicatable<T, Indicator<T>> {

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Function<Tick, Tick> normalizer) {
        super(normalizer);
    }

    /**
     * Build with the delegation {@link Indicator}.
     * 
     * @param indicator A {@link Indicator} to delegate.
     */
    protected Indicator(Indicator indicator) {
        super(indicator.normalizer);
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
                if (count == 0) return Indicator.this.valueAtRounded(tick);
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
    public static NumIndicator averageTrueRange(Ticker ticker, int size) {
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
    public static NumIndicator trueRange(Ticker ticker) {
        return NumIndicator.build(ticker, tick -> {
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
