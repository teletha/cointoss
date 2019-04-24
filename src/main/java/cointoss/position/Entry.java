/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.position;

import java.util.LinkedList;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.order.Order;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public class Entry implements Directional {

    /** Then entry holder. */
    private final LinkedList<Order> entryOrders = new LinkedList();

    private Signaling<Order> entries = new Signaling();

    /** The entry info. */
    public final Variable<Num> entrySize = entries.expose.map(v -> v.size).scanWith(Num.ZERO, Num::plus).to();

    /** The entry info. */
    public final Variable<Num> entryRemainingSize = entries.expose.flatMap(v -> diff(v.remainingSize)).scanWith(Num.ZERO, Num::plus).to();

    /** The entry info. */
    public final Variable<Num> entryExecutedSize = entries.expose.flatMap(v -> diff(v.executedSize)).scanWith(Num.ZERO, Num::plus).to();

    /** The entry info. */
    public final Variable<Num> entryPice = entries.expose.flatMap(v -> diff(v.cost))
            .scanWith(Num.ZERO, Num::plus)
            .combineLatest(entryExecutedSize.observeNow())
            .map(v -> v.ⅱ.isZero() ? Num.ZERO : v.ⅰ.divide(v.ⅱ))
            .to();

    /** Then exit holder. */
    private final LinkedList<Order> exitOrders = new LinkedList();

    private Signaling<Order> exits = new Signaling();

    /** The exit info. */
    public final Variable<Num> exitSize = exits.expose.map(v -> v.size).scanWith(Num.ZERO, Num::plus).to();

    /** The exit info. */
    public final Variable<Num> exitRemainingSize = exits.expose.flatMap(v -> diff(v.remainingSize)).scanWith(Num.ZERO, Num::plus).to();

    /** The exit info. */
    public final Variable<Num> exitExecutedSize = exits.expose.flatMap(v -> diff(v.executedSize)).scanWith(Num.ZERO, Num::plus).to();

    /** The exit info. */
    public final Variable<Num> exitPice = exits.expose.flatMap(v -> diff(v.cost))
            .scanWith(Num.ZERO, Num::plus)
            .combineLatest(exitExecutedSize.observeNow())
            .map(v -> v.ⅱ.isZero() ? Num.ZERO : v.ⅰ.divide(v.ⅱ))
            .to();

    /** The position info. */
    public final Variable<Num> positionSize = entryExecutedSize.observe()
            .combineLatest(exitExecutedSize.observe())
            .startWith(I.pair(Num.ZERO, Num.ZERO))
            .map(v -> v.ⅰ.minus(v.ⅱ))
            .to();

    /**
     * Add entry order.
     * 
     * @param order
     */
    public void addEntry(Order order) {
        entryOrders.add(order);
        entries.accept(order);
    }

    /**
     * Add exit order.
     * 
     * @param order
     */
    public void addExit(Order order) {
        exitOrders.add(order);
        exits.accept(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return entryOrders.peekFirst().direction();
    }

    static Signal<Num> diff(Variable<Num> value) {
        return value.observeNow().maps(Num.ZERO, (prev, now) -> now.minus(prev));
    }
}
