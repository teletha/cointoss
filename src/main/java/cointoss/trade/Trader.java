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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import cointoss.Direction;
import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Signaling;

public abstract class Trader {

    /** The market. */
    protected final Market market;

    /** The signal observers. */
    final Signaling<Boolean> closePositions = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> closingPosition = closePositions.expose;

    /** The signal observers. */
    final Signaling<Boolean> completeEntries = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = completeEntries.expose;

    /** The signal observers. */
    final Signaling<Boolean> completeExits = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = completeExits.expose;

    /** The user setting. */
    protected Num maxPositionSize = Num.of(1);

    /** All managed entries. */
    public final Deque<Entry> entries = new ArrayDeque<>();

    /*** All active posiitons. */
    final List<Entry> actives = new ArrayList();

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    protected Trader(Market market) {
        this.market = Objects.requireNonNull(market);
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
     * Create entry with the specified order.
     * 
     * @param order
     * @param entry
     */
    protected final void entry(Order order, Trading entry) {
        if (order == null) {
            return;
        }

        entry.createEntry(NewEntry.with.trader(this).order(order));
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

        return new Entry(this, Order.with.direction(side, size).price(price), process);
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
        return new Entry(this, Order.with.direction(side, size), process);
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
}
