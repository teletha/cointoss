/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Indicator<T> extends AbstractIndicator<T, Indicator<T>> {
    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Ticker ticker) {
        this(ticker, tick -> {
            Tick rounded = ticker.ticks.at(tick.openTime);
            return rounded == null ? ticker.ticks.firstCache() : rounded;
        });
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicator(Ticker ticker, Function<Tick, Tick> normalizer) {
        super(ticker, normalizer);
    }

    /**
     * Return the value of this {@link Indicator}.
     * 
     * @param timestamp A {@link Tick} on {@link Ticker}.
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
    protected Indicator<T> build(BiFunction<Tick, Indicator<T>, T> delegator) {
        return new Indicator<>(ticker, normalizer) {

            @Override
            protected T valueAtRounded(Tick tick) {
                return delegator.apply(tick, this);
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
            Tick rounded = ticker.ticks.at(tick.openTime);
            return rounded == null ? ticker.ticks.firstCache() : rounded;
        };

        return new Indicator<>(ticker, normalizer) {

            @Override
            protected T valueAtRounded(Tick tick) {
                return calculator.apply(tick);
            }
        };
    }
}