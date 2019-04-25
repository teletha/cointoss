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
import cointoss.order.OrderManager;
import cointoss.util.Num;
import cointoss.util.NumVar;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public class Entry implements Directional {

    /** Then entry holder. */
    private final LinkedList<Order> entryOrders = new LinkedList();

    private Signaling<Order> entries = new Signaling();

    /** The entry info. */
    public final NumVar entrySize = entries.expose.map(v -> v.size).scanWith(Num.ZERO, Num::plus).to(NumVar.class, NumVar::set);

    /** The entry info. */
    public final NumVar entryRemainingSize = entries.expose.flatMap(v -> diff(v.remainingSize))
            .scanWith(Num.ZERO, Num::plus)
            .to(NumVar.class, NumVar::set);

    /** The entry info. */
    public final NumVar entryExecutedSize = entries.expose.flatMap(v -> diff(v.executedSize))
            .scanWith(Num.ZERO, Num::plus)
            .startWith(Num.ZERO)
            .to(NumVar.class, NumVar::set);

    /** The entry info. */
    public final NumVar entryPice = entries.expose.flatMap(v -> diff(v.cost))
            .scanWith(Num.ZERO, Num::plus)
            .combineLatest(entryExecutedSize.observeNow())
            .map(v -> v.ⅱ.isZero() ? Num.ZERO : v.ⅰ.divide(v.ⅱ))
            .to(NumVar.class, NumVar::set);

    /** Then exit holder. */
    private final LinkedList<Order> exitOrders = new LinkedList();

    private Signaling<Order> exits = new Signaling();

    /** The exit info. */
    public final NumVar exitSize = exits.expose.map(v -> v.size).scanWith(Num.ZERO, Num::plus).to(NumVar.class, NumVar::set);

    /** The exit info. */
    public final NumVar exitRemainingSize = exits.expose.flatMap(v -> diff(v.remainingSize))
            .scanWith(Num.ZERO, Num::plus)
            .to(NumVar.class, NumVar::set);

    /** The exit info. */
    public final NumVar exitExecutedSize = exits.expose.flatMap(v -> diff(v.executedSize))
            .scanWith(Num.ZERO, Num::plus)
            .startWith(Num.ZERO)
            .to(NumVar.class, NumVar::set);

    /** The exit info. */
    public final NumVar exitPice = exits.expose.flatMap(v -> diff(v.cost))
            .scanWith(Num.ZERO, Num::plus)
            .combineLatest(exitExecutedSize.observeNow())
            .map(v -> v.ⅱ.isZero() ? Num.ZERO : v.ⅰ.divide(v.ⅱ))
            .to(NumVar.class, NumVar::set);

    /** The position info. */
    public final NumVar positionSize = entryExecutedSize.observeNow()
            .combineLatest(exitExecutedSize.observeNow())
            .startWith(I.pair(Num.ZERO, Num.ZERO))
            .map(v -> v.ⅰ.minus(v.ⅱ))
            .to(NumVar.class, NumVar::set);

    private final OrderManager service;

    /**
     * Create new {@link Entry}.
     * 
     * @param service
     * @param entry
     */
    public Entry(OrderManager service, Order entry) {
        this.service = service;
        addEntry(entry);
    }

    /**
     * Add entry order.
     * 
     * @param order
     */
    public Entry addEntry(Order order) {
        entryOrders.add(order);
        entries.accept(order);

        return this;
    }

    /**
     * Add exit order.
     * 
     * @param order
     */
    public Entry addExit(Order order) {
        exitOrders.add(order);
        exits.accept(order);

        return this;
    }

    /**
     * Cancel all entry orders.
     */
    public void cancelEntry() {
        for (Order entry : entryOrders) {
            if (entry.isNotCanceled() && entry.isNotCompleted()) {
                service.cancel(entry).to(order -> {
                    Num remaining = entryRemainingSize.set(v -> v.minus(order.remainingSize));
                    entrySize.set(v -> v.minus(remaining));
                });
            }
        }
    }

    public Signal<Entry> requestExit(Order order) {
        return service.request(order).map(exit -> {
            addExit(exit);
            return this;
        });
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
