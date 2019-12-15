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

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import org.eclipse.collections.api.block.function.primitive.ObjectDoubleToDoubleFunction;
import org.eclipse.collections.api.block.function.primitive.ObjectDoubleToObjectFunction;

import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;

public abstract class Indicatable<T> {

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
    public final <With, Out> Indicator<Out> map(Indicator<With> combinator, WiseBiFunction<T, With, Out> mapper) {
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
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <Out> Indicator<Out> map(DoubleIndicator combinator, ObjectDoubleToObjectFunction<T, Out> mapper) {
        return new Indicator<Out>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper.valueOf(Indicatable.this.valueAt(timestamp), combinator.doubleAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.valueOf(Indicatable.this.valueAt(tick), combinator.doubleAt(tick));
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
    public final <With1, With2, Out> Indicator<Out> map(Indicatable<With1> combinator1, Indicatable<With2> combinator2, WiseTriFunction<T, With1, With2, Out> mapper) {
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
    public final <With> DoubleIndicator dmap(Indicator<With> combinator, ToDoubleBiFunction<T, With> mapper) {
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
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param <Out> A result type.
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With> DoubleIndicator dmap(DoubleIndicator combinator, ObjectDoubleToDoubleFunction<T> mapper) {
        return new DoubleIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Double valueAt(Tick timestamp) {
                return mapper.valueOf(Indicatable.this.valueAt(timestamp), combinator.doubleAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.valueOf(Indicatable.this.valueAt(tick), combinator.doubleAt(tick));
            }
        };
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> observeWhen(Signal<Tick> timing) {
        return timing.map(this::valueAt);
    }

}
