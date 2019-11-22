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
    private static final Snapshot EMPTY_SNAPSHOT = new Snapshot(Num.ZERO, Num.ZERO, Num.ZERO);

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
    public final Profitable snapshotAt(ZonedDateTime time) {
        return snapshots.floorEntry(time).getValue();
    }

    /**
     * Create the snapshot of trading log.
     * 
     * @return
     */
    public final TradingLog log() {
        return new TradingLog(market, funds, scenarios, this);
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
    final void updateSnapshot(Num deltaRealizedProfit, Num deltaRemainingSize, Num price) {
        Snapshot letest = snapshots.lastEntry().getValue();

        ZonedDateTime now = market.service.now().plus(59, SECONDS).truncatedTo(MINUTES);
        Num newRealized = letest.realizedProfit.plus(deltaRealizedProfit);
        Num newSize = letest.remainingSize.plus(deltaRemainingSize);
        Num newPrice = newSize.isZero() ? Num.ZERO
                : price == null ? letest.entryPrice
                        : letest.remainingSize.multiply(letest.entryPrice).plus(deltaRemainingSize.multiply(price)).divide(newSize);
        snapshots.put(now, new Snapshot(newRealized, newPrice, newSize));

        // update holding size
        setHoldSize(newSize);
        if (newSize.abs().isGreaterThan(holdMaxSize)) {
            setHoldMaxSize(newSize.abs());
        }

        // update profit
        Direction direction = newSize.isPositive() ? Direction.BUY : Direction.SELL;
        setProfit(newRealized.plus(market.tickers.latestPrice.v.diff(direction, newPrice).multiply(newSize.abs())));
    }

    /**
     * The snapshot of {@link Trader}'s state.
     */
    private static class Snapshot implements Profitable {

        /** The realized profit. */
        private final Num realizedProfit;

        /** The entry price. */
        private final Num entryPrice;

        /** The entry size which is . */
        private final Num remainingSize;

        /**
         * Store the current state.
         * 
         * @param realizedProfit
         * @param entryPrice
         * @param entryExecutedUnexitedSize
         */
        private Snapshot(Num realizedProfit, Num entryPrice, Num remainingSize) {
            this.realizedProfit = realizedProfit;
            this.entryPrice = entryPrice;
            this.remainingSize = remainingSize;
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
            Direction direction = remainingSize.isPositive() ? Direction.BUY : Direction.SELL;
            return price.diff(direction, entryPrice).multiply(remainingSize.abs());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num entryRemainingSize() {
            return remainingSize;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Snapshot [realizedProfit=" + realizedProfit + ", entryPrice=" + entryPrice + ", remainingSize=" + remainingSize + "]";
        }
    }
}
