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
public abstract class TradingStrategy {

    /** The market. */
    protected final Market market;

    /** The signal of closing position. */
    protected final Signal<Boolean> closingPosition;

    /** The signal observers. */
    private final CopyOnWriteArrayList<Observer<Boolean>> closePositions = new CopyOnWriteArrayList();

    /** The signal of closing position. */
    protected final Signal<Boolean> completingEntry;

    /** The signal observers. */
    private final CopyOnWriteArrayList<Observer<Boolean>> completeEntries = new CopyOnWriteArrayList();

    /** The user setting. */
    protected Decimal maxPositionSize = Decimal.ONE;

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

    /** The entry order. */
    private Order entry;

    /**
     * New Trade.
     */
    protected TradingStrategy(Market market) {
        this.market = market;
        this.closingPosition = new Signal(closePositions);
        this.completingEntry = new Signal(completeEntries);
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
    protected final void entryLimit(Side side, Decimal size, Decimal price, Consumer<Order> process) {
        if (position == null || position == side) {
            position = side;
            requestEntrySize = requestEntrySize.plus(size);

            market.request(Order.limit(side, size, price)).to(o -> {
                entry = o;
                process.accept(o);

                market.timeline.takeUntil(closingPosition).to(e -> {
                    managePosition(o, e, true);
                });
            });
        }
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final void entryMarket(Side side, Decimal size, Consumer<Order> process) {
        requestEntrySize = requestEntrySize.plus(size);

        market.request(Order.market(side, size)).to(o -> {
            entry = o;
            process.accept(o);

            market.timeline.takeUntil(closingPosition).to(e -> {
                managePosition(o, e, true);
            });
        });
    }

    /**
     * Request entry order.
     * 
     * @param size
     */
    protected final void exitMarket(Decimal size) {
        if (hasPosition()) {
            requestExitSize = requestExitSize.plus(size);

            market.request(Order.market(position.inverse(), size).with(entry)).to(o -> {
                market.timeline.takeUntil(closingPosition).to(e -> {
                    managePosition(o, e, false);
                });
            });
        }
    }

    /**
     * Close entry and position.
     * 
     * @param oae
     */
    protected final void close(OrderAndExecution oae) {
        market.cancel(oae.order).to(id -> {

        });
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
                    last.set(e.exec_date.plus(time, unit));
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
     * This method is called from instantiating to clearing position.
     * 
     * @param exe
     */
    public abstract void timeline(Execution exe);

    /**
     * Write your trading rule. This method is called from instantiating to clearing position.
     * 
     * @param exe
     */
    void tick(Execution exe) {
        timeline(exe);
    }
}