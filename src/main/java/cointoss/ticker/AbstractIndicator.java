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

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import hypatia.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;
import kiss.Ⅱ;
import kiss.Ⅲ;
import primavera.function.ToDoubleTriFunction;

public abstract class AbstractIndicator<T, Self extends AbstractIndicator<T, Self>> {

    /** The human-readable name. */
    public final Variable<String> name = Variable.of(getClass().getSimpleName());

    /** The datastore. */
    protected final Ticker ticker;

    /** The mapper from timestamp to tick. */
    protected final Function<Tick, Tick> normalizer;

    /** The state of memoization. */
    boolean memoized = false;

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected AbstractIndicator(Ticker ticker) {
        this(ticker, null);
    }

    /**
     * Build with the target {@link Ticker}.
     * 
     * @param ticker A target ticker.
     */
    protected AbstractIndicator(Ticker ticker, Function<Tick, Tick> normalizer) {
        this.ticker = ticker;
        this.normalizer = normalizer == null ? Function.identity() : normalizer;
    }

    /**
     * Creates a new {@link AbstractIndicator} of the same type as itself.
     * 
     * @param delegator Actual process with recursive call helper.
     * @return New created {@link AbstractIndicator}.
     */
    protected abstract Self build(BiFunction<Tick, Self, T> delegator);

    /**
     * Set this indicator's name.
     * 
     * @param name A indicator name to set.
     * @return Chainable API.
     */
    public Self name(Object name) {
        this.name.set(String.valueOf(name));
        return (Self) this;
    }

    /**
     * Return the value of this {@link Indicator}.
     * 
     * @param tick A {@link Tick} on {@link Ticker}.
     * @return A time-based value.
     */
    public abstract T valueAt(Tick tick);

    /**
     * Acquires the latest value of this {@link Indicator}.
     * 
     * @return
     */
    public final T valueAtLast() {
        return valueAt(ticker.current);
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> valueAt(Ticker ticker) {
        return valueAt(ticker.open);
    }

    /**
     * Acquires the stream in which the indicator value flows at the specified timing.
     * 
     * @return
     */
    public final Signal<T> valueAt(Signal<Tick> ticker) {
        return ticker.map(this::valueAt);
    }

    /**
     * Detects the first tick that meets the conditions in reverse order from the tail end.
     * 
     * @param condition
     * @return
     */
    public final Tick findLatest(BiPredicate<Tick, T> condition) {
        return ticker.ticks.before(ticker.current, tick -> condition.test(tick, valueAt(tick)));
    }

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
     * @param <With1> First combination type.
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
        return new Indicator<>(ticker, normalizer) {
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
        return new Indicator<Out>(ticker, normalizer) {
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
        return new Indicator<Out>(ticker, normalizer) {
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
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final DoubleIndicator dmap(ToDoubleFunction<T> mapper) {
        return new DoubleIndicator(ticker, normalizer) {
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
        return new DoubleIndicator(ticker, normalizer) {
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
        return new DoubleIndicator(ticker, normalizer) {
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
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final NumIndicator nmap(Function<T, Num> mapper) {
        return new NumIndicator(ticker, normalizer) {
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
     * @param <With> A result type.
     * @param combinator First Combinator.
     * @param mapper A mapping function.
     * @return Mapped indicator.
     */
    public final <With> NumIndicator nmap(AbstractIndicator<With, ?> combinator, WiseBiFunction<T, With, Num> mapper) {
        return new NumIndicator(ticker, normalizer) {
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
        return new NumIndicator(ticker, normalizer) {
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

        Cache<Tick, T> cache = CacheBuilder.newBuilder().maximumSize(64).weakKeys().weakValues().build();
        int[] count = {limit};

        Self memo = build((tick, created) -> {
            if (count[0] == 0) return valueAt(tick);
            if (tick.ticker != null /* The latest tick MUST NOT cache. */) return calculator.apply(tick, created::valueAt);

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
}