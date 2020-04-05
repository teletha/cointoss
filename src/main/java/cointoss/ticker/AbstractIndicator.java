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

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cointoss.util.Num;
import cointoss.util.ToDoubleTriFunction;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;

public abstract class AbstractIndicator<T, Self extends AbstractIndicator<T, Self>> {

    /** The human-readable name. */
    public final Variable<String> name = Variable.of(getClass().getSimpleName());

    /** The mapper from timestamp to tick. */
    protected final Function<Tick, Tick> normalizer;

    /** The state of memoization. */
    boolean memoized = false;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected AbstractIndicator() {
        this(null);
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected AbstractIndicator(Function<Tick, Tick> normalizer) {
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
     * Creates a new {@link AbstractIndicator} of the same type as itself.
     * 
     * @param delegator Actual process with recursive call helper.
     * @return New created {@link AbstractIndicator}.
     */
    protected abstract Self build(BiFunction<Tick, Self, T> delegator);

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
    public final <With> Indicator<Ⅱ<T, With>> combine(AbstractIndicator<With, ?> combinator) {
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
    public final <With1, With2> Indicator<Ⅲ<T, With1, With2>> combine(AbstractIndicator<With1, ?> combinator1, AbstractIndicator<With2, ?> combinator2) {
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
                return mapper.apply(AbstractIndicator.this.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick));
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
    public final <With, Out> Indicator<Out> map(AbstractIndicator<With, ?> combinator, WiseBiFunction<T, With, Out> mapper) {
        return new Indicator<Out>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper.apply(AbstractIndicator.this.valueAt(timestamp), combinator.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick), combinator.valueAt(tick));
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
    public final <With1, With2, Out> Indicator<Out> map(AbstractIndicator<With1, ?> combinator1, AbstractIndicator<With2, ?> combinator2, WiseTriFunction<T, With1, With2, Out> mapper) {
        return new Indicator<Out>(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Out valueAt(Tick timestamp) {
                return mapper
                        .apply(AbstractIndicator.this.valueAt(timestamp), combinator1.valueAt(timestamp), combinator2.valueAt(timestamp));
            }

            @Override
            protected Out valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick), combinator1.valueAt(tick), combinator2.valueAt(tick));
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
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With> DoubleIndicator dmap(AbstractIndicator<With, ?> combinator, ToDoubleBiFunction<T, With> mapper) {
        return new DoubleIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Double valueAt(Tick timestamp) {
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(timestamp), combinator.valueAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(tick), combinator.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param combinator1 First Combinator.
     * @param combinator2 Second Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With1, With2> DoubleIndicator dmap(AbstractIndicator<With1, ?> combinator1, AbstractIndicator<With2, ?> combinator2, ToDoubleTriFunction<T, With1, With2> mapper) {
        return new DoubleIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Double valueAt(Tick timestamp) {
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(timestamp), combinator1.valueAt(timestamp), combinator2
                        .valueAt(timestamp));
            }

            @Override
            protected double valueAtRounded(Tick tick) {
                return mapper.applyAsDouble(AbstractIndicator.this.valueAt(tick), combinator1.valueAt(tick), combinator2.valueAt(tick));
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
    public final NumIndicator nmap(Function<T, Num> mapper) {
        return new NumIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Num valueAt(Tick timestamp) {
                return mapper.apply(AbstractIndicator.this.valueAt(timestamp));
            }

            @Override
            protected Num valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick));
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
    public final <With> NumIndicator nmap(AbstractIndicator<With, ?> combinator, WiseBiFunction<T, With, Num> mapper) {
        return new NumIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Num valueAt(Tick timestamp) {
                return mapper.apply(AbstractIndicator.this.valueAt(timestamp), combinator.valueAt(timestamp));
            }

            @Override
            protected Num valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick), combinator.valueAt(tick));
            }
        };
    }

    /**
     * Gets the indicator whose value is changed by the mapping function.
     * 
     * @param combinator1 First Combinator.
     * @param combinator2 Second Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With1, With2> NumIndicator nmap(AbstractIndicator<With1, ?> combinator1, AbstractIndicator<With2, ?> combinator2, WiseTriFunction<T, With1, With2, Num> mapper) {
        return new NumIndicator(normalizer) {
            @Override // override to avoid unnecessary calculations
            public Num valueAt(Tick timestamp) {
                return mapper
                        .apply(AbstractIndicator.this.valueAt(timestamp), combinator1.valueAt(timestamp), combinator2.valueAt(timestamp));
            }

            @Override
            protected Num valueAtRounded(Tick tick) {
                return mapper.apply(AbstractIndicator.this.valueAt(tick), combinator1.valueAt(tick), combinator2.valueAt(tick));
            }
        };
    }

    /**
     * Get the indicator that the calculation result for each tick is memoized.
     * 
     * @return Memoized {@link AbstractIndicator}.
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
     * @return Memoized {@link AbstractIndicator}.
     */
    public final Self memoize(int limit, BiFunction<Tick, Function<Tick, T>, T> calculator) {
        if (memoized) {
            return (Self) this;
        }

        Cache<Tick, T> cache = CacheBuilder.newBuilder().maximumSize(8192 * 4).weakKeys().weakValues().build();
        int[] count = {limit};

        Self memo = build((tick, created) -> {
            if (count[0] == 0) return valueAt(tick);
            if (tick.closePrice == null /* The latest tick MUST NOT cache. */) return calculator.apply(tick, created::valueAt);

            try {
                return cache.get(tick, () -> calculator.apply(tick, t -> {
                    count[0] = count[0] - 1;
                    T v = created.valueAt(t);
                    count[0] = count[0] + 1;
                    return v;
                }));
            } catch (ExecutionException e) {
                throw I.quiet(e);
            }
        });

        memo.memoized = true;
        return memo;
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> observeWhen(Signal<Tick> timing) {
        return timing.map(this::valueAt);
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> updateBy(Ticker ticker) {
        return updateBy(ticker.open);
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> updateBy(Signal<Tick> ticker) {
        return ticker.map(this::valueAt);
    }
}
