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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderStrategy;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.Num;
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
    private final LinkedList<Entry> entries = new LinkedList<>();

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
    protected final Entry latest() {
        return entries.peekLast();
    }

    /**
     * Set up entry at your timing.
     * 
     * @param <T>
     * @param timing
     * @param builder
     */
    protected final <T> void when(Signal<T> timing, Function<T, Entry> builder) {
        if (timing == null || builder == null) {
            return;
        }

        disposer.add(timing.takeWhile(v -> enable.get()).to(value -> {
            Entry entry = builder.apply(value);

            if (entry != null) {
                entries.add(entry);

                entry.order();
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
     * Declarative entry and exit definition.
     */
    public abstract class Entry extends EntryStatus implements Directional {

        /** The entry direction. */
        protected final Direction direction;

        /** The fund management for this entry. */
        protected final FundManager funds;

        protected final StopLoss stopLoss = new StopLoss();

        /** The list entry orders. */
        private final List<Order> entries = new ArrayList<>();

        /** The list exit orders. */
        private final List<Order> exits = new ArrayList<>();

        /**
         * @param directional
         */
        protected Entry(Directional directional) {
            this(directional, null);
        }

        /**
         * @param directional
         */
        protected Entry(Directional directional, FundManager funds) {
            this.direction = directional.direction();
            this.funds = funds == null ? Trader.this.funds : funds;
        }

        /**
         * Declare entry order.
         */
        protected abstract void order();

        /**
         * We will order with the specified quantity. Use the return the {@link Takable} &
         * {@link Makable} value to define the details of the ordering method.
         * 
         * @param <S> Ordering interface
         * @param size A entry size.
         * @return A ordering method.
         */
        protected final <S extends Takable & Makable> void order(long size, Consumer<S> declaration) {
            order(Num.of(size), declaration);
        }

        /**
         * We will order with the specified quantity. Use the return the {@link Takable} &
         * {@link Makable} value to define the details of the ordering method.
         * 
         * @param <S> Ordering interface
         * @param size A entry size.
         * @return A ordering method.
         */
        protected final <S extends Takable & Makable> void order(double size, Consumer<S> declaration) {
            order(Num.of(size), declaration);
        }

        /**
         * We will order with the specified quantity. Use the return the {@link Takable} &
         * {@link Makable} value to define the details of the ordering method.
         * 
         * @param <S> Ordering interface
         * @param size A entry size.
         * @return A ordering method.
         */
        protected final <S extends Takable & Makable> void order(Num size, Consumer<S> declaration) {
            if (size == null || size.isLessThan(market.service.setting.targetCurrencyMinimumBidSize)) {
                throw new Error("Entry size is less than minimum bid size.");
            }
            market.request(direction, size, declaration).to(this::processAddEntryOrder);
        }

        /**
         * Process for additional entry order.
         * 
         * @param order
         */
        private void processAddEntryOrder(Order order) {
            entries.add(order);
            setEntrySize(entrySize.plus(order.size));

            order.observeExecutedSize().to(v -> {
                updateOrderRelatedStatus(entries, this::setEntryPrice, this::setEntryExecutedSize);
            });
        }

        /**
         * Calculate average price and total executed size.
         * 
         * @param orders
         */
        private void updateOrderRelatedStatus(List<Order> orders, Consumer<Num> priceSetter, Consumer<Num> sizeSetter) {
            Num totalSize = Num.ZERO;
            Num totalPrice = Num.ZERO;

            for (Order order : orders) {
                totalSize = totalSize.plus(order.executedSize);
                totalPrice = totalPrice.plus(order.executedSize.multiply(order.price));
            }

            sizeSetter.accept(totalSize);
            priceSetter.accept(totalPrice.divide(totalSize));
        }

        /**
         * Declare exit order.
         */
        protected void fixProfit() {
            fixProfitAtRiskRewardRatio();
        }

        /**
         * Declare exit order
         * 
         * @param <S>
         * @param timing
         * @param strategy
         */
        protected final <S extends Takable & Makable> void fixProfitWhen(Signal<?> timing, Consumer<S> strategy) {

        }

        /**
         * 
         * 
         */
        protected final void fixProfitAtRiskRewardRatio() {

        }

        /**
         * Declare exit orders. Loss cutting is the only element in the trade that investors can
         * control.
         */
        protected void stopLoss() {
            stopLossAtAcceptableRisk();
        }

        /**
         * Declare exit order.
         */
        protected final void stopLossAtAcceptableRisk() {
            stop.when(profit.observeNow().take(v -> v.isLessThan(funds.riskAssets().negate()))).how(OrderStrategy.with.take());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Direction direction() {
            return direction;
        }

        // /**
        // * {@inheritDoc}
        // */
        // @Override
        // public String toString() {
        // return new StringBuilder() //
        // .append("注文 ")
        // .append(holdTime())
        // .append("\t 損益")
        // .append(profit().asJPY(4))
        // .append("\t")
        // .append(exitSize())
        // .append("/")
        // .append(order.executedSize)
        // .append("@")
        // .append(direction().mark())
        // .append(entryPrice().asJPY(1))
        // .append(" → ")
        // .append(exitPrice().asJPY(1))
        // .toString();
        // }

    }
}
