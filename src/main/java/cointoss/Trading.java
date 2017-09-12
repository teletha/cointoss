/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import eu.verdelhan.ta4j.Decimal;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/09/05 19:39:34
 */
public abstract class Trading {

    /** The market. */
    protected final Market market;

    /** The signal observers. */
    private final CopyOnWriteArrayList<Observer<Boolean>> closePositions = new CopyOnWriteArrayList();

    /** The trade related signal. */
    protected final Signal<Boolean> closingPosition = new Signal(closePositions);

    /** The signal observers. */
    private final CopyOnWriteArrayList<Observer<Boolean>> completeEntries = new CopyOnWriteArrayList();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = new Signal(completeEntries);

    /** The signal observers. */
    private final CopyOnWriteArrayList<Observer<Boolean>> completeExits = new CopyOnWriteArrayList();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = new Signal(completeExits);

    /** The user setting. */
    protected Decimal maxPositionSize = Decimal.valueOf(0.1);

    /** The current position. (null means no position) */
    protected Side position;

    /** The current requested entry size. */
    protected Decimal requestEntrySize = Decimal.ZERO;

    /** The current requested exit size. */
    protected Decimal requestExitSize = Decimal.ZERO;

    /** The current position size. */
    protected Decimal positionSize = Decimal.ZERO;

    /** The current position average price. */
    protected Decimal positionPrice = Decimal.ZERO;

    /** All managed entries. */
    protected final List<Entry> entries = new ArrayList<>();

    /**
     * New Trade.
     */
    protected Trading(Market market) {
        this.market = market;
        this.market.tradings.add(this);

        closingPosition.to(() -> {
            position = null;
            requestEntrySize = Decimal.ZERO;
            requestExitSize = Decimal.ZERO;
            positionSize = Decimal.ZERO;
            positionPrice = Decimal.ZERO;
        });
    }

    /**
     * Helper to check position state.
     * 
     * @return
     */
    protected final boolean hasPosition() {
        return !requestEntrySize.isZero() || !requestExitSize.isZero() || !positionSize.isZero();
    }

    /**
     * Helper to check position state.
     * 
     * @return
     */
    protected final boolean hasNoPosition() {
        return requestEntrySize.isZero() && requestExitSize.isZero() && positionSize.isZero();
    }

    /**
     * Helper to check position state.
     * 
     * @return
     */
    protected final boolean hasEntry() {
        return !positionSize.isZero();
    }

    /**
     * Helper to check position state.
     * 
     * @return
     */
    protected final boolean hasExit() {
        return !requestExitSize.isZero();
    }

    /**
     * Calculate current profit or loss.
     * 
     * @return
     */
    protected final Decimal profit() {
        return market.getLatestPrice().minus(positionPrice).multipliedBy(positionSize);
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final Entry entryLimit(Side side, Decimal size, Decimal price, Consumer<Entry> process) {
        // check side
        if (side == null) {
            return null;
        }

        // check size
        if (size == null || size.isLessThanOrEqual(Decimal.ZERO)) {
            return null;
        }

        // check price
        if (price == null || price.isLessThanOrEqual(Decimal.ZERO)) {
            return null;
        }
        return entry(Order.limit(side, size, price), process);
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final void entryMarket(Side side, Decimal size, Consumer<Entry> process) {
        // check side
        if (side == null) {
            return;
        }

        // check size
        if (size == null || size.isLessThanOrEqual(Decimal.ZERO)) {
            return;
        }
        entry(Order.market(side, size), process);
    }

    /**
     * <p>
     * Request entry order.
     * </p>
     * 
     * @param order
     * @param process
     */
    private Entry entry(Order order, Consumer<Entry> process) {
        // update trade state
        position = order.side();
        requestEntrySize = requestEntrySize.plus(order.size());

        // create new entry
        Entry entry = new Entry(order);
        entries.add(entry);

        // request order
        market.request(order).to(o -> {
            AtomicBoolean first = new AtomicBoolean(true);

            o.notify(exe -> {
                managePosition(o, exe, true);

                if (process != null && first.getAndSet(false)) {
                    process.accept(entry);
                }

                if (o.isCompleted()) {
                    for (Observer<Boolean> observer : completeEntries) {
                        observer.accept(true);
                    }
                }
            });
        });
        return entry;
    }

    /**
     * Manage position.
     * 
     * @param oae
     */
    private void managePosition(Order order, Execution exe, boolean entry) {
        if (exe.isMine()) {
            if (entry) {
                requestEntrySize = requestEntrySize.minus(exe.size);
            } else {
                requestExitSize = requestExitSize.minus(exe.size);
            }

            Decimal size = positionSize;

            // update position
            if (position == null) {
                // new position
                position = order.side();
                positionSize = exe.size;
                positionPrice = exe.price;
            } else if (position == order.side()) {
                // same position
                positionSize = positionSize.plus(exe.size);
                positionPrice = positionPrice.multipliedBy(size).plus(exe.price.multipliedBy(exe.size)).dividedBy(positionSize);
            } else {
                // counter position
                positionSize = positionSize.minus(exe.size);

                if (positionSize.isZero()) {
                    // clear position
                    position = null;
                    positionPrice = Decimal.ZERO;

                    for (Observer<Boolean> observer : closePositions) {
                        observer.accept(true);
                    }
                } else if (positionSize.isNegative()) {
                    // inverse position
                    position = position.inverse();
                    positionPrice = exe.price;
                } else {
                    // decrease position
                    positionPrice = positionPrice.multipliedBy(size).minus(exe.price.multipliedBy(exe.size)).dividedBy(positionSize);
                }
            }
        }
    }

    /**
     * Close entry and position.
     * 
     * @param oae
     */
    protected final void close() {
        for (Observer<Boolean> observer : closePositions) {
            observer.accept(true);
        }
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
                    if (e.exec_date.isAfter(last.get())) {
                        testing.set(false);
                        return true;
                    }
                } else {
                    testing.set(true);
                    last.set(e.exec_date.plus(time, unit).minusNanos(1));
                }
            } else {
                if (testing.get()) {
                    if (e.exec_date.isAfter(last.get())) {
                        testing.set(false);
                    }
                }
            }
            return false;
        };
    }

    /**
     * Cancel entry.
     * 
     * @param entry
     */
    protected final void cancel(Entry entry) {
        if (entry != null && entry.order.isNotCompleted()) {
            market.cancel(entry.order).to(id -> {
                close();
            });
        }
    }

    /**
     * @version 2017/09/11 16:57:47
     */
    public class Entry implements Directional {

        /** The entry order. */
        final Order order;

        /** The list exit orders. */
        final List<Order> exit = new ArrayList<>();

        /** The detail log. */
        final List<String> logs = new ArrayList();

        /**
         * Create {@link Entry} with {@link Order}.
         * 
         * @param entry A entry order.
         */
        private Entry(Order entry) {
            this.order = entry;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Side side() {
            return order.side();
        }

        /**
         * @return
         */
        public Decimal remaining() {
            return order.outstanding_size;
        }

        /**
         * @return
         */
        public Decimal executed() {
            return order.executed_size;
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         * @param price A exit price.
         */
        protected final void exitLimit(Decimal size, Decimal price, Consumer<Order> process) {
            // check size
            if (size == null || size.isLessThanOrEqual(Decimal.ZERO)) {
                return;
            }

            // check price
            if (price == null || price.isLessThanOrEqual(Decimal.ZERO)) {
                return;
            }
            exit(Order.limit(position.inverse(), size, price), process);
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        protected final void exitMarket() {
            // check size
            exitMarket(executed());

            if (!remaining().isZero()) {
                market.cancel(order).to();
            }
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        protected final void exitMarket(Decimal size) {
            // check size
            if (size == null || size.isLessThanOrEqual(Decimal.ZERO)) {
                return;
            }
            exit(Order.market(position.inverse(), size), null);
        }

        /**
         * Request exit order.
         * 
         * @param order A exit order.
         */
        private void exit(Order order, Consumer<Order> process) {
            if (hasPosition()) {
                requestExitSize = requestExitSize.plus(order.size());

                market.request(order).to(o -> {
                    exit.add(o);

                    if (process != null) {
                        process.accept(o);
                    }

                    o.notify(exe -> {
                        managePosition(o, exe, false);

                        if (o.isCompleted()) {
                            for (Observer<Boolean> observer : completeExits) {
                                observer.accept(true);
                            }
                        }
                    });
                });
            }
        }

        /**
         * <p>
         * Write detail log.
         * </p>
         * 
         * @param message
         * @param params
         */
        protected void log(String message, Object... params) {
            logs.add(String.format(message, params));
        }
    }
}