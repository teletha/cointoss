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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
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
import cointoss.order.OrderState;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.Chrono;
import cointoss.util.LinkedQueue;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
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
     * Create the trading log snapshot.
     * 
     * @return
     */
    public TradingLog log() {
        return new TradingLog(market, funds, entries);
    }

    /**
     * Declarative entry and exit definition.
     */
    public abstract class Entry extends EntryStatus implements Directional {

        /** The entry direction. */
        protected final Direction direction;

        /** The fund management for this entry. */
        protected final FundManager funds;

        /** The list entry orders. */
        private final LinkedQueue<Order> entries = new LinkedQueue<>();

        /** The list exit orders. */
        private final LinkedQueue<Order> exits = new LinkedQueue<>();

        /** The exit disposer. */
        private final Disposable diposer = Disposable.empty();

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

            disposer.add(observeEntryExecutedSize().first().to(this::exit));

            // calculate profit
            observeExitExecutedSize().effectOnce(this::disposeEntries).to(size -> {
                setRealizedProfit(exitPrice.diff(direction, entryPrice).multiply(size));
            }, diposer);
        }

        private void disposeEntries() {
            I.signal(entries).take(o -> o.isNotCompleted() && o.isNotCanceled()).flatMap(market::cancel).to(o -> {

            });
        }

        /**
         * Check {@link OrderState} of this entry.
         * 
         * @return A result.
         */
        public final boolean isActive() {
            return isTerminated() == false;
        }

        /**
         * Check {@link OrderState} of this entry.
         * 
         * @return A result.
         */
        public final boolean isTerminated() {
            return isEntryTerminated() && isExitTerminated();
        }

        /**
         * Check {@link OrderState} of this entry.
         * 
         * @return A result.
         */
        public final boolean isEntryTerminated() {
            return entries.stream().allMatch(Order::isTerminated);
        }

        /**
         * Check {@link OrderState} of this entry.
         * 
         * @return A result.
         */
        public final boolean isExitTerminated() {
            return exits.isEmpty() == false && exits.stream().allMatch(Order::isTerminated);
        }

        /**
         * Compute position holding time.
         */
        public final Duration holdTime() {
            ZonedDateTime start = entries.first().flatMap(o -> o.executions.first()).map(Execution::date).or(Chrono.MIN);
            ZonedDateTime end = exits.last().flatMap(o -> o.executions.last()).map(Execution::date).or(market.service.now());

            return Duration.between(start, end);
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
        protected final void order(long size, Consumer<Orderable> declaration) {
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
        protected final void order(double size, Consumer<Orderable> declaration) {
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
        protected final void order(Num size, Consumer<Orderable> declaration) {
            if (size == null || size.isLessThan(market.service.setting.targetCurrencyMinimumBidSize)) {
                throw new Error("Entry size is less than minimum bid size.");
            }
            System.out.println("orders");
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

            priceSetter.accept(totalPrice.divide(totalSize));
            sizeSetter.accept(totalSize);
        }

        /**
         * Declare exit order. Loss cutting is the only element in the trade that investors can
         * control.
         */
        protected void exit() {
            exitAtRiskRewardRatio();
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(long price) {
            exitAt(Num.of(price));
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(double price) {
            exitAt(Num.of(price));
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(Num price) {
            if (price.isGreaterThan(direction, entryPrice)) {
                observeEntryExecutedSizeDiff().to(size -> {
                    market.request(direction.inverse(), size, s -> s.make(price)).to(this::processAddExitOrder);
                });
            } else {
                market.tickers.latest.observe().take(e -> e.price.isLessThanOrEqual(direction, price)).first().to(e -> {
                    market.request(direction.inverse(), entryExecutedSize.minus(exitExecutedSize), s -> s.take())
                            .to(this::processAddExitOrder);
                });
            }
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(long price, Consumer<Orderable> strategy) {
            exitAt(Num.of(price), strategy);
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(double price, Consumer<Orderable> strategy) {
            exitAt(Num.of(price), strategy);
        }

        /**
         * Declare exit order by price. Loss cutting is the only element in the trade that investors
         * can control.
         * 
         * @param price An exit price.
         */
        protected final void exitAt(Num price, Consumer<Orderable> strategy) {
            if (price.isGreaterThan(direction, entryPrice)) {
                market.tickers.latest.observe().take(e -> e.price.isGreaterThanOrEqual(direction, price)).first().to(e -> {
                    market.request(direction.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processAddExitOrder);
                });
            } else {
                market.tickers.latest.observe().take(e -> e.price.isLessThanOrEqual(direction, price)).first().to(e -> {
                    market.request(direction.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processAddExitOrder);
                });
            }
        }

        /**
         * Declare exit order
         * 
         * @param <S>
         * @param timing
         * @param strategy
         */
        protected final void exitWhen(Signal<?> timing, Consumer<Orderable> strategy) {
            disposer.add(timing.first().to(() -> {
                market.request(direction.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processAddExitOrder);
            }));
        }

        /**
         * Process for additional exit order.
         * 
         * @param order
         */
        private void processAddExitOrder(Order order) {
            exits.add(order);
            setExitSize(exitSize.plus(order.size));

            order.observeExecutedSize().to(v -> {
                updateOrderRelatedStatus(exits, this::setExitPrice, this::setExitExecutedSize);
            });
        }

        /**
         * 
         * 
         */
        protected final void exitAtRiskRewardRatio() {

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
