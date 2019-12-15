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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;

public abstract class Indicatable<T, Self extends Indicatable<T, Self>> {

    /** The human-readable name. */
    public final Variable<String> name = Variable.of(getClass().getSimpleName());

    /** The mapper from timestamp to tick. */
    protected final Function<Tick, Tick> normalizer;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicatable() {
        this(null);
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected Indicatable(Function<Tick, Tick> normalizer) {
        this.normalizer = normalizer == null ? Function.identity() : normalizer;
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
     * Return the value of this {@link Indicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    public abstract T valueAt(Tick timestamp);

    /**
     * Gets the indicator whose value is paired with the combinator.
     * 
     * @param <With> First combination type.
     * @param combinator First Combinator.
     * @return Combined indicator.
     */
    public final <With> Indicator<Ⅱ<T, With>> combine(Indicatable<With, ?> combinator) {
        return map(combinator, (a, b) -> I.pair(a, b));
    }

    /**
     * Gets the indicator whose value is paired with the combinator.
     * 
     * @param <With> First combination type.
     * @param combinator1 First Combinator.
     * @param combinator2 Second Combinator.
     * @return Combined indicator.
     */
    public final <With1, With2> Indicator<Ⅲ<T, With1, With2>> combine(Indicatable<With1, ?> combinator1, Indicatable<With2, ?> combinator2) {
        return map(combinator1, combinator2, (a, b, c) -> I.pair(a, b, c));
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <Out> Indicator<Out> map(Function<T, Out> mapper) {
        return new Indicator<>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper.apply(Indicatable.this.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(Indicatable.this.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With, Out> Indicator<Out> map(Indicatable<With, ?> combinator, WiseBiFunction<T, With, Out> mapper) {
        return new Indicator<Out>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper.apply(Indicatable.this.valueAt(timestamp), combinator.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(Indicatable.this.valueAt(tick), combinator.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param combinator1 First Combinator.
     * @param combinator2 Second Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With1, With2, Out> Indicator<Out> map(Indicatable<With1, ?> combinator1, Indicatable<With2, ?> combinator2, WiseTriFunction<T, With1, With2, Out> mapper) {
        return new Indicator<Out>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper.apply(Indicatable.this.valueAt(timestamp), combinator1.valueAt(timestamp), combinator2.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(Indicatable.this.valueAt(tick), combinator1.valueAt(tick), combinator2.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final DoubleIndicator dmap(ToDoubleFunction<T> mapper) {
        return new DoubleIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Double valueAt(Tick timestamp) {
                return mapper.applyAsDouble(Indicatable.this.valueAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.applyAsDouble(Indicatable.this.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With> DoubleIndicator dmap(Indicatable<With, ?> combinator, ToDoubleBiFunction<T, With> mapper) {
        return new DoubleIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Double valueAt(Tick timestamp) {
                return mapper.applyAsDouble(Indicatable.this.valueAt(timestamp), combinator.valueAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.applyAsDouble(Indicatable.this.valueAt(tick), combinator.valueAt(tick));
            }
        };
    }

    /**
     * Get the indicator that the calculation result for each tick is memoized.
     * 
     * @return Memoized {@link Indicatable}.
     */
    public final Self memoize() {
        return memoize(1, (tick, self) -> valueAt(tick));
    }

    /**
     * Get the indicator that the calculation result for each tick is memoized. It also has a helper
     * function that allows recursive calls.
     * 
     * @param limit A maximum depth of recursive calls.
     * @param calculator A memoize function with recursive call helper.
     * @return Memoized {@link Indicatable}.
     */
    public abstract Self memoize(int limit, BiFunction<Tick, Function<Tick, T>, T> calculator);

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> observeWhen(Signal<Tick> timing) {
        return timing.map(this::valueAt);
    }

}
