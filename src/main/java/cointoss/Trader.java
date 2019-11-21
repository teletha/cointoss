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
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.collections.impl.list.mutable.FastList;

import com.google.common.annotations.VisibleForTesting;

import cointoss.execution.Execution;
import cointoss.ticker.Indicator;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.util.Chrono;
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

    /** The state snapshot. */
    @VisibleForTesting
    final NavigableMap<ZonedDateTime, Snapshot> snapshots = new TreeMap<>(Map.of(Chrono.MIN, EMPTY_SNAPSHOT));

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
        return snapshots.floorEntry(time).getValue();
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

    /**
     * Create state snapshot.
     * 
     * @return
     */
    void snapshot(Num increasedRealizedProfit, Num increasedRemainingSize, Num increasedPrice) {
        Snapshot previous = snapshots.lastEntry().getValue();

        ZonedDateTime now = market.service.now().plus(59, SECONDS).truncatedTo(MINUTES);
        Num realizedProfit = previous.realizedProfit.plus(increasedRealizedProfit);
        Num remainingSize = previous.remainingSize.plus(increasedRemainingSize);
        Num entryPrice = remainingSize.isZero() ? Num.ZERO
                : increasedPrice != null
                        ? previous.remainingSize.multiply(previous.entryPrice)
                                .plus(increasedRemainingSize.multiply(increasedPrice))
                                .divide(remainingSize)
                        : previous.entryPrice;

        snapshots.put(now, new Snapshot(null, realizedProfit, entryPrice, remainingSize));
    }

    static final Snapshot EMPTY_SNAPSHOT = new Snapshot(Direction.BUY, Num.ZERO, Num.ZERO, Num.ZERO);

    /**
     * 
     */
    private static class Snapshot implements Profitable {

        /** The direction. */
        private final Direction direction;

        /** The realized profit. */
        private final Num realizedProfit;

        /** The entry price. */
        private final Num entryPrice;

        /** The entry size which is . */
        private final Num remainingSize;

        /**
         * @param realizedProfit
         * @param entryPrice
         * @param entryExecutedUnexitedSize
         */
        private Snapshot(Direction direction, Num realizedProfit, Num entryPrice, Num remainingSize) {
            this.direction = direction;
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
            return price.diff(remainingSize.isPositive() ? Direction.BUY : Direction.SELL, entryPrice).multiply(remainingSize);
        }
    }
}
