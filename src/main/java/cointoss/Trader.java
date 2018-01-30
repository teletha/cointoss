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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import cointoss.order.Order;
import cointoss.util.Listeners;
import cointoss.util.Num;
import cointoss.util.Span;
import kiss.Disposable;
import kiss.Signal;

/**
 * @version 2017/09/05 19:39:34
 */
public abstract class Trader implements Disposable {

    /** The market. */
    protected Market market;

    /** The signal observers. */
    private final Listeners<Boolean> closePositions = new Listeners();

    /** The trade related signal. */
    protected final Signal<Boolean> closingPosition = new Signal(closePositions);

    /** The signal observers. */
    private final Listeners<Boolean> completeEntries = new Listeners();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = new Signal(completeEntries);

    /** The signal observers. */
    private final Listeners<Boolean> completeExits = new Listeners();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = new Signal(completeExits);

    /** The user setting. */
    protected Num maxPositionSize = Num.of(1);

    /** All managed entries. */
    public final Deque<Entry> entries = new ArrayDeque<>();

    /*** All active posiitons. */
    private final List<Entry> actives = new ArrayList();

    /**
     * Initialize this trading strategy.
     */
    protected abstract void initialize();

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        // do nothing
    }

    /**
     * Detect position.
     * 
     * @return
     */
    protected final boolean hasPosition() {
        return actives.isEmpty() == false;
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
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final Entry entryLimit(Side side, Num size, Num price, Consumer<Entry> process) {
        // check side
        if (side == null) {
            return null;
        }

        // check size
        if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
            return null;
        }

        // check price
        if (price == null || price.isLessThanOrEqual(Num.ZERO)) {
            return null;
        }

        return new Entry(Order.limit(side, size, price), process);
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final Entry entryMarket(Side side, Num size, Consumer<Entry> process) {
        // check side
        if (side == null) {
            return null;
        }

        // check size
        if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
            return null;
        }
        return new Entry(Order.market(side, size), process);
    }

    /**
     * Close entry and position.
     * 
     * @param oae
     */
    protected final void close() {
        closePositions.accept(true);
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
     * Cancel entry.
     * 
     * @param order
     */
    protected final void cancel(Order order) {
        if (order != null && order.isNotCompleted()) {
            market.cancel(order).to(id -> {

            });
        }
    }

    /**
     * @version 2017/09/17 19:59:43
     */
    public class Entry implements Directional {

        /** The entry order. */
        final Order order;

        /** The list exit orders. */
        final List<Order> exit = new ArrayList<>();

        /** The detail log. */
        final List<String> logs = new ArrayList();

        /** The current position size. */
        private Num positionSize = Num.ZERO;

        /** The remaining size of entry order. */
        private Num entryRemaining;

        /** The total size of entry order. */
        private Num entryTotalSize = Num.ZERO;

        /** The total cost of entry order. */
        private Num entryCost = Num.ZERO;

        /** The remaining size of entry order. */
        private Num exitRemaining = Num.ZERO;

        /** The total size of exit order. */
        private Num exitTotalSize = Num.ZERO;

        /** The total cost of exit order. */
        private Num exitCost = Num.ZERO;

        /**
         * Create {@link Entry} with {@link Order}.
         * 
         * @param entry A entry order.
         */
        private Entry(Order entry, Consumer<Entry> initializer) {
            this.order = entry;
            this.entryRemaining = entry.size;

            // create new entry
            entries.add(this);
            actives.add(this);

            // request order
            market.request(order).to(o -> {
                o.execute.to(exe -> {
                    positionSize = positionSize.plus(exe.size);
                    entryTotalSize = entryTotalSize.plus(exe.size);
                    entryRemaining = entryRemaining.minus(exe.size);
                    entryCost = entryCost.plus(exe.price.multiply(exe.size));

                    if (o.isCompleted()) {
                        completeEntries.accept(true);
                    }
                });
                if (initializer != null) initializer.accept(this);
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Side side() {
            return order.side();
        }

        /**
         * Calculate remaining size of position.
         * 
         * @return
         */
        public final Num remaining() {
            return positionSize;
        }

        /**
         * Calculate profit or loss.
         * 
         * @return
         */
        public final Num profit() {
            Num up, down;

            if (side().isBuy()) {
                up = exitCost.plus(positionSize.multiply(market.latestPrice.v));
                down = entryCost;
            } else {
                up = entryCost;
                down = exitCost.plus(positionSize.multiply(market.latestPrice.v));
            }
            return up.minus(down);
        }

        /**
         * Calculate total executed entry size.
         * 
         * @return
         */
        public final Num entrySize() {
            return entryTotalSize;
        }

        /**
         * Calculate average of entry price.
         * 
         * @return
         */
        public final Num entryPrice() {
            return entryTotalSize.isZero() ? Num.ZERO : entryCost.divide(entryTotalSize);
        }

        /**
         * Calculate total executed exit size.
         * 
         * @return
         */
        public final Num exitSize() {
            return exitTotalSize;
        }

        /**
         * Calculate average of exit price.
         * 
         * @return
         */
        public final Num exitPrice() {
            return exitTotalSize.isZero() ? Num.ZERO : exitCost.divide(exitTotalSize);
        }

        /**
         * Calculate ordering time.
         * 
         * @return
         */
        public final Span orderTime() {
            Execution last = order.executions.peekLast();
            ZonedDateTime start = order.child_order_date.get();
            ZonedDateTime finish = last == null ? market.getExecutionLatest().exec_date : last.exec_date;

            if (start.isBefore(finish)) {
                finish = market.getExecutionLatest().exec_date;
            }
            return new Span(start, finish);
        }

        /**
         * Calculate holding time.
         * 
         * @return
         */
        public final Span holdTime() {
            Execution first = order.executions.peekFirst();

            if (first == null) {
                return Span.ZERO;
            }

            ZonedDateTime start = first.exec_date;
            ZonedDateTime finish = start;

            if (isActive()) {
                finish = market.getExecutionLatest().exec_date;
            } else {
                for (Order order : exit) {
                    Execution last = order.executions.peekLast();

                    if (last != null) {
                        finish = last.exec_date;
                    }
                }
            }

            // if (start.isBefore(finish)) {
            // finish = market.getExecutionLatest().exec_date;
            // }

            if (finish.isBefore(start)) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                for (Execution e : order.executions) {
                    System.out.println("Start Exe " + e);
                }

                for (Order o : exit) {
                    for (Execution e : o.executions) {
                        System.out.println("Exit Exe " + e);
                    }
                }

                throw new Error(finish + "   " + start);
            }

            return new Span(start, finish);
        }

        /**
         * Cehck whether this position has profit
         */
        public final boolean isWin() {
            return profit().isPositive();
        }

        /**
         * Cehck whether this position has loss
         */
        public final boolean isLose() {
            return profit().isNegative();
        }

        /**
         * Cehck whether this position is not activated.
         */
        public final boolean isInitial() {
            return order.size.is(entryRemaining);
        }

        /**
         * Cehck whether this position was activated but not completed.
         */
        public final boolean isActive() {
            return positionSize.isZero() == false;
        }

        /**
         * Cehck whether this position was completed.
         */
        public final boolean isCompleted() {
            return positionSize.isZero() && entryRemaining.isZero();
        }

        /**
         * Cehck whether this position was not activated, then it was canceled.
         */
        public final boolean isCanceled() {
            return isInitial() && order.isCanceled();
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         * @param price A exit price.
         */
        public final void exitLimit(Num size, Num price, Consumer<Order> process) {
            // check size
            if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
                return;
            }

            // check price
            if (price == null || price.isLessThanOrEqual(Num.ZERO)) {
                return;
            }
            exit(Order.limit(order.inverse(), size, price), process);
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        public final void exitMarket() {
            exitMarket((Consumer<Order>) null);
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        public final void exitMarket(Consumer<Order> process) {
            // check size
            exitMarket(remaining(), process);

            if (!remaining().isZero()) {
                market.cancel(order).to();
            }
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        public final void exitMarket(Num size) {
            exitMarket(size, null);
        }

        /**
         * Request exit order.
         * 
         * @param size A exit size.
         */
        public final void exitMarket(Num size, Consumer<Order> process) {
            // check size
            if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
                return;
            }
            exit(Order.market(order.inverse(), size), process);
        }

        /**
         * Request exit order.
         * 
         * @param order A exit order.
         */
        private void exit(Order order, Consumer<Order> initializer) {
            exitRemaining = exitRemaining.plus(order.size);

            market.request(order).to(o -> {
                exit.add(o);

                o.execute.to(exe -> {
                    positionSize = positionSize.minus(exe.size);
                    exitTotalSize = exitTotalSize.plus(exe.size);
                    exitRemaining = exitRemaining.minus(exe.size);
                    exitCost = exitCost.plus(exe.price.multiply(exe.size));

                    if (o.isCompleted()) {
                        completeExits.accept(true);

                        if (positionSize.isZero()) {
                            actives.remove(this);
                            closePositions.accept(true);
                        }
                    }
                });
                if (initializer != null) initializer.accept(o);
            });
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

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new StringBuilder() //
                    .append("注文 ")
                    .append(holdTime())
                    .append("\t 損益")
                    .append(profit().asJPY(4))
                    .append("\t")
                    .append(exitSize())
                    .append("/")
                    .append(entrySize())
                    .append("@")
                    .append(side().mark())
                    .append(entryPrice().asJPY(1))
                    .append(" → ")
                    .append(exitPrice().asJPY(1))
                    .toString();
        }
    }
}