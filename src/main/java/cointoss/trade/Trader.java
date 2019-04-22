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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.Order.Relations;
import cointoss.util.Num;
import cointoss.util.Span;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2017/09/05 19:39:34
 */
public abstract class Trader implements Disposable {

    /** The market. */
    protected Market market;

    /** The signal observers. */
    private final Signaling<Boolean> closePositions = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> closingPosition = closePositions.expose;

    /** The signal observers. */
    private final Signaling<Boolean> completeEntries = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = completeEntries.expose;

    /** The signal observers. */
    private final Signaling<Boolean> completeExits = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = completeExits.expose;

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
     * Detect position state.
     * 
     * @return
     */
    protected final boolean hasPosition() {
        return actives.isEmpty() == false;
    }

    /**
     * Detect position state.
     * 
     * @return
     */
    protected final boolean hasNotPosition() {
        return actives.isEmpty() == true;
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
    protected final Entry entryLimit(Direction side, Num size, Num price, Consumer<Entry> process) {
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

        return new Entry(Order.of(side, size).price(price), process);
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final Entry entryMarket(Direction side, Num size, Consumer<Entry> process) {
        // check side
        if (side == null) {
            return null;
        }

        // check size
        if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
            return null;
        }
        return new Entry(Order.of(side, size), process);
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
        public final Order order;

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
                o.executed.to(exe -> {
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
        public Direction direction() {
            return order.direction();
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

            if (direction().isBuy()) {
                up = exitCost.plus(positionSize.multiply(market.tickers.latest.v.price));
                down = entryCost;
            } else {
                up = entryCost;
                down = exitCost.plus(positionSize.multiply(market.tickers.latest.v.price));
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
            Variable<Execution> last = order.attribute(Relations.class).last();
            ZonedDateTime start = order.creationTime.get();
            ZonedDateTime finish = last.map(v -> v.date).or(market.tickers.latest.v.date);

            if (start.isBefore(finish)) {
                finish = market.tickers.latest.v.date;
            }
            return new Span(start, finish);
        }

        /**
         * Calculate holding time.
         * 
         * @return
         */
        public final Span holdTime() {
            Variable<Execution> first = order.attribute(Relations.class).first();

            if (first.isAbsent()) {
                return Span.ZERO;
            }

            ZonedDateTime start = first.v.date;
            ZonedDateTime finish = start;

            if (isActive()) {
                finish = market.tickers.latest.v.date;
            } else {
                for (Order order : exit) {
                    Variable<Execution> last = order.attribute(Relations.class).last();

                    if (last.isPresent()) {
                        finish = last.v.date;
                    }
                }
            }

            // if (start.isBefore(finish)) {
            // finish = market.getExecutionLatest().exec_date;
            // }

            if (finish.isBefore(start)) {
                // If this exception will be thrown, it is bug of this program. So we must rethrow
                // the wrapped error in here.
                order.attribute(Relations.class).all().to(e -> {
                    System.out.println("Start Exe " + e);
                });

                for (Order o : exit) {
                    o.attribute(Relations.class).all().to(e -> {
                        System.out.println("Exit Exe " + e);
                    });
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
            exit(Order.of(order.inverse(), size).price(price), process);
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
            exit(Order.of(order.inverse(), size), process);
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

                o.executed.to(exe -> {
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
        public void log(String message, Object... params) {
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
                    .append(direction().mark())
                    .append(entryPrice().asJPY(1))
                    .append(" → ")
                    .append(exitPrice().asJPY(1))
                    .toString();
        }
    }
}
