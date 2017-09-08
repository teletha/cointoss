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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/05 19:39:34
 */
public abstract class TradingStrategy {

    /** The market. */
    protected final Market market;

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
    protected final List<Order> entryOrders = new CopyOnWriteArrayList();

    /** The entry order. */
    protected final List<Order> exitOrders = new CopyOnWriteArrayList();

    /**
     * New Trade.
     */
    protected TradingStrategy(Market market) {
        this.market = market;
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
    protected final void entryLimit(Side side, Decimal size, Decimal price) {
        requestEntrySize = requestEntrySize.plus(size);

        Order.limit(side, size, price).entryTo(market).to(e -> managePosition(e, true));
    }

    /**
     * Request entry order.
     * 
     * @param side
     * @param size
     */
    protected final void entryMarket(Side side, Decimal size) {
        requestEntrySize = requestEntrySize.plus(size);

        Order.market(side, size).entryTo(market).to(e -> managePosition(e, true));
    }

    /**
     * Request entry order.
     * 
     * @param size
     */
    protected final void exitMarket(Decimal size) {
        if (hasPosition()) {
            requestExitSize = requestExitSize.plus(size);

            // Order.market(position.inverse(), size).with(entry).entryTo(market).to(e ->
            // managePosition(e, false));
        }
    }

    /**
     * Manage position.
     * 
     * @param oae
     */
    private void managePosition(OrderAndExecution oae, boolean entry) {
        Execution exe = oae.e;

        if (exe.isMine()) {
            Decimal size = positionSize;

            // update position
            if (position == null) {
                // new position
                position = oae.o.side();
                positionSize = exe.size;
                positionPrice = exe.price;
            } else if (position == oae.o.side()) {
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
     * Write your entry rule. This method is called whenever this trade has no position.
     * 
     * @param exe
     */
    public abstract void tryEntry(Execution exe);

    /**
     * Write your exit rule. This method is called whenever this trade has some position.
     * 
     * @param exe
     */
    public abstract void tryExit(Execution exe);

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
        boolean entry = !requestEntrySize.isZero();
        boolean exit = !requestExitSize.isZero();
        boolean position = !positionSize.isZero();

        // entry and exit timing
        if (!entry && !exit && !position) {
            tryEntry(exe);
        } else if (position && requestExitSize.isLessThan(positionSize)) {
            tryExit(exe);
        }

        // observe timeline
        timeline(exe);
    }
}