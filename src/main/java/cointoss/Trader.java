/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.collections.impl.list.mutable.FastList;

import cointoss.execution.Execution;
import cointoss.ticker.Indicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.Signal;
import kiss.WiseFunction;
import kiss.WiseSupplier;

public abstract class Trader implements Profitable {

    /** The market. */
    protected final Market market;

    /** The fund management. */
    protected final FundManager funds;

    /** All managed entries. */
    final FastList<Scenario> scenarios = new FastList();

    /** The alive state. */
    private final AtomicBoolean enable = new AtomicBoolean(true);

    /** The disposer manager. */
    private final Disposable disposer = Disposable.empty();

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    public Trader(Market market) {
        this.market = Objects.requireNonNull(market);
        this.market.managedTraders.add(this);
        this.funds = FundManager.with.totalAssets(market.service.baseCurrency().first().to().v);
    }

    /**
     * Set up entry at your timing.
     * 
     * @param <T>
     * @param timing
     * @param builder
     * @return Chainable API.
     */
    public final <T> Trader when(Signal<T> timing, WiseSupplier<Scenario> builder) {
        return when(timing, v -> builder.get());
    }

    /**
     * Set up entry at your timing.
     * 
     * @param <T>
     * @param timing
     * @param builder
     * @return Chainable API.
     */
    public final <T> Trader when(Signal<T> timing, WiseFunction<T, Scenario> builder) {
        Objects.requireNonNull(timing);
        Objects.requireNonNull(builder);

        disposer.add(timing.takeWhile(v -> enable.get()).to(value -> {
            Scenario scenario = builder.apply(value);

            if (scenario != null) {
                scenario.market = market;
                scenario.funds = funds;
                scenario.entry();

                if (scenario.entries.isEmpty() == false) {
                    scenarios.add(scenario);
                }
            }
        }));
        return this;
    }

    /**
     * A realized profit or loss of this entry.
     * 
     * @return A realized profit or loss of this entry.
     */
    @Override
    public final Num realizedProfit() {
        return scenarios.stream().map(Scenario::realizedProfit).reduce(Num.ZERO, Num::plus);
    }

    /**
     * Calculate unrealized profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return An unrealized profit or loss of this entry.
     */
    @Override
    public final Num unrealizedProfit(Num currentPrice) {
        return scenarios.stream().map(s -> s.unrealizedProfit(currentPrice)).reduce(Num.ZERO, Num::plus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profitable snapshotAt(ZonedDateTime time) {
        return new Profitable() {

            @Override
            public Num unrealizedProfit(Num currentPrice) {
                Num value = Num.ZERO;
                for (Scenario scenario : scenarios) {
                    value = value.plus(scenario.snapshotAt(time).unrealizedProfit(currentPrice));
                }
                return value;
            }

            @Override
            public Num realizedProfit() {
                Num value = Num.ZERO;
                for (Scenario scenario : scenarios) {
                    value = value.plus(scenario.snapshotAt(time).realizedProfit());
                }
                return value;
            }
        };
    }

    /**
     * Create the snapshot of trading log.
     * 
     * @return
     */
    public final TradingLog log() {
        return new TradingLog(market, funds, scenarios);
    }

    /**
     * Build your {@link Indicator}.
     * 
     * @param <T>
     * @param span
     * @param calculator
     * @return
     */
    protected final Indicator indicator(Span span, Function<Tick, Num> calculator) {
        return Indicator.build(market.tickers.of(span), calculator);
    }

    /**
     * Create rule which the specified condition is fulfilled during the specified duration.
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    public static final Predicate<Execution> keep(int time, TemporalUnit unit, BooleanSupplier condition) {
        return keep(time, unit, e -> condition.getAsBoolean());
    }

    /**
     * Create rule which the specified condition is fulfilled during the specified duration.
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    public static final Predicate<Execution> keep(int time, TemporalUnit unit, Predicate<Execution> condition) {
        AtomicBoolean testing = new AtomicBoolean();
        AtomicReference<ZonedDateTime> last = new AtomicReference(ZonedDateTime.now());

        return e -> {
            if (condition.test(e)) {
                if (testing.get()) {
                    if (e.date.isAfter(last.get())) {
                        testing.set(false);
                        return true;
                    }
                } else {
                    testing.set(true);
                    last.set(e.date.plus(time, unit).minusNanos(1));
                }
            } else {
                if (testing.get()) {
                    if (e.date.isAfter(last.get())) {
                        testing.set(false);
                    }
                }
            }
            return false;
        };
    }
}
