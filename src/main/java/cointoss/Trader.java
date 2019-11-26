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

import static java.time.temporal.ChronoUnit.*;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.list.mutable.FastList;

import com.google.common.annotations.VisibleForTesting;

import cointoss.analyze.TradingStatistics;
import cointoss.execution.Execution;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.WiseFunction;
import kiss.WiseSupplier;

public abstract class Trader extends TraderBase {

    /** The identity element of {@link Snapshot}. */
    private static final Snapshot EMPTY_SNAPSHOT = new Snapshot(Num.ZERO, Num.ZERO, Num.ZERO, Num.ZERO, Num.ZERO);

    /** The market. */
    protected final Market market;

    /** The fund management. */
    protected final FundManager funds;

    /** All managed entries. */
    private final FastList<Scenario> scenarios = new FastList();

    /** The disposer manager. */
    private final Disposable disposer = Disposable.empty();

    /** The state snapshot. */
    private final NavigableMap<ZonedDateTime, Snapshot> snapshots = new TreeMap();

    /** The trader's alive state. */
    private Set<Signal> disable = Sets.mutable.empty();

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    public Trader(Market market) {
        this.market = Objects.requireNonNull(market);
        this.market.managedTraders.add(this);
        this.funds = FundManager.with.totalAssets(market.service.baseCurrency().first().to().v);

        initialize();
    }

    /**
     * The human-readable identical name.
     * 
     * @return An identical trader name.
     */
    public String name() {
        return getClass().getSimpleName();
    }

    /**
     * Initialize status
     */
    @VisibleForTesting
    void initialize() {
        scenarios.clear();
        setHoldSize(Num.ZERO);
        setHoldMaxSize(Num.ZERO);
        snapshots.clear();
        snapshots.put(Chrono.MIN, EMPTY_SNAPSHOT);
        disable.clear();
    }

    /**
     * Retrieve the latest {@link Scenario}.
     */
    @VisibleForTesting
    Scenario latest() {
        return scenarios.getLast();
    }

    /**
     * Retrieve the latest {@link Snapshot}.
     */
    @VisibleForTesting
    Profitable snapshotLatest() {
        return snapshots.lastEntry().getValue();
    }

    /**
     * Make this {@link Trader} enable forcibly.
     */
    protected final void enable() {
        disable.clear();
    }

    /**
     * Make this {@link Trader} disable forcibly.
     */
    protected final void disable() {
        disable.add(I.signal());
    }

    /**
     * Check whether this {@link Trader} is active or not.
     * 
     * @return it returns true if this {@link Trader} is active.
     */
    protected final boolean isEnable() {
        return disable.isEmpty();
    }

    /**
     * Check whether this {@link Trader} is disactive or not.
     * 
     * @return it returns true if this {@link Trader} is disactive.
     */
    protected final boolean isDisable() {
        return !disable.isEmpty();
    }

    /**
     * Disable this {@link Trader} while the specified duration.
     * 
     * @param duration
     */
    protected final void disableWhile(Signal<Boolean> duration) {
        disposer.add(duration.to(condition -> {
            if (condition) {
                disable.add(duration);
            } else {
                disable.remove(duration);
            }
        }).add(() -> disable.remove(duration)));
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

        disposer.add(timing.take(disable::isEmpty).to(value -> {
            Scenario scenario = builder.apply(value);

            if (scenario != null) {
                scenario.trader = this;
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
     * Calcualte the sanpshot when market is the specified datetime and price.
     * 
     * @param time The specified date and time.
     * @return A snapshot of this {@link Scenario}.
     */
    public final Snapshot snapshotAt(ZonedDateTime time) {
        return snapshots.floorEntry(time).getValue();
    }

    /**
     * Create the current trading statistics.
     * 
     * @return A current statistics.
     */
    public final TradingStatistics statistics() {
        return new TradingStatistics(market, funds, scenarios, this);
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

    /**
     * Update snapshot.
     * 
     * @param deltaRealizedProfit
     * @param deltaRemainingSize
     * @param price
     */
    final void updateSnapshot(Direction direction, Num deltaRealizedProfit, Num deltaRemainingSize, Num price) {
        Snapshot latest = snapshots.lastEntry().getValue();

        ZonedDateTime now = market.service.now().plus(59, SECONDS).truncatedTo(MINUTES);
        Num newRealized = latest.realizedProfit.plus(deltaRealizedProfit);
        Snapshot snapshot;

        if (direction == Direction.BUY) {
            Num newLongSize = latest.longSize.plus(deltaRemainingSize);
            Num newLongPrice = newLongSize.isZero() ? Num.ZERO
                    : price == null ? latest.longPrice
                            : latest.longSize.multiply(latest.longPrice).plus(deltaRemainingSize.multiply(price)).divide(newLongSize);

            snapshot = new Snapshot(newRealized, newLongPrice, newLongSize, latest.shortPrice, latest.shortSize);
        } else {
            Num newShortSize = latest.shortSize.plus(deltaRemainingSize);
            Num newShortPrice = newShortSize.isZero() ? Num.ZERO
                    : price == null ? latest.shortPrice
                            : latest.shortSize.multiply(latest.shortPrice).plus(deltaRemainingSize.multiply(price)).divide(newShortSize);

            snapshot = new Snapshot(newRealized, latest.longPrice, latest.longSize, newShortPrice, newShortSize);
        }
        snapshots.put(now, snapshot);

        // update holding size
        Num newHoldSize = snapshot.entryRemainingSize();
        setHoldSize(newHoldSize);
        if (newHoldSize.abs().isGreaterThan(holdMaxSize)) {
            setHoldMaxSize(newHoldSize.abs());
        }

        // update profit
        setProfit(snapshot.profit(market.tickers.latestPrice.v));
    }

    /**
     * The snapshot of {@link Trader}'s state.
     */
    public static class Snapshot implements Profitable {

        /** The realized profit. */
        private final Num realizedProfit;

        /** The average long price. */
        private final Num longPrice;

        /** The average short price. */
        private final Num shortPrice;

        /** The long size. */
        public final Num longSize;

        /** The short size. */
        public final Num shortSize;

        /**
         * Store the current state.
         * 
         * @param realizedProfit
         * @param longPrice
         * @param longSize
         * @param shortPrice
         * @param shortSize
         */
        private Snapshot(Num realizedProfit, Num longPrice, Num longSize, Num shortPrice, Num shortSize) {
            this.realizedProfit = realizedProfit;
            this.longPrice = longPrice;
            this.longSize = longSize;
            this.shortPrice = shortPrice;
            this.shortSize = shortSize;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num realizedProfit() {
            return realizedProfit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num unrealizedProfit(Num price) {
            Num longProfit = price.diff(Direction.BUY, longPrice).multiply(longSize);
            Num shortProfit = price.diff(Direction.SELL, shortPrice).multiply(shortSize);
            return longProfit.plus(shortProfit);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num entryRemainingSize() {
            return longSize.minus(shortSize);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Snapshot [realizedProfit=" + realizedProfit + ", longPrice=" + longPrice + ", shortPrice=" + shortPrice + ", longSize=" + longSize + ", shortSize=" + shortSize + "]";
        }
    }
}
