/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

import cointoss.Market;
import cointoss.execution.Execution;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

public abstract class Trader {

    /** The market. */
    protected final Market market;

    /** The fund management. */
    protected final FundManager funds;

    /** The signal observers. */
    final Signaling<Boolean> completeEntries = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = completeEntries.expose;

    /** The signal observers. */
    final Signaling<Boolean> completeExits = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = completeExits.expose;

    /** All managed entries. */
    private final LinkedList<Scenario> entries = new LinkedList<>();

    /** The alive state. */
    private final AtomicBoolean enable = new AtomicBoolean(true);

    /** The disposer manager. */
    private final Disposable disposer = Disposable.empty();

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    protected Trader(Market market) {
        this.market = Objects.requireNonNull(market);
        this.funds = FundManager.with.totalAssets(market.service.baseCurrency().first().to().v);
    }

    /**
     * Return the latest completed or canceled entry.
     * 
     * @return
     */
    protected final Scenario latest() {
        return entries.peekLast();
    }

    /**
     * Set up entry at your timing.
     * 
     * @param <T>
     * @param timing
     * @param builder
     */
    protected final <T> void when(Signal<T> timing, Function<T, Scenario> builder) {
        if (timing == null || builder == null) {
            return;
        }

        disposer.add(timing.takeWhile(v -> enable.get()).to(value -> {
            Scenario scenario = builder.apply(value);

            if (scenario != null) {
                scenario.market = market;
                scenario.funds = funds;
                scenario.entry();

                if (scenario.entries.isEmpty() == false) {
                    entries.add(scenario);
                }
            }
        }));
    }

    /**
     * <p>
     * Create rule which the specified condition is fulfilled during the specified duration.
     * </p>
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    protected final Predicate<Execution> keep(int time, TemporalUnit unit, BooleanSupplier condition) {
        return keep(time, unit, e -> condition.getAsBoolean());
    }

    /**
     * <p>
     * Create rule which the specified condition is fulfilled during the specified duration.
     * </p>
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    protected final Predicate<Execution> keep(int time, TemporalUnit unit, Predicate<Execution> condition) {
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

    /**
     * Create the trading log snapshot.
     * 
     * @return
     */
    public TradingLog log() {
        return new TradingLog(market, funds, entries);
    }
}
