/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.order.Order;
import cointoss.util.ObservableNumProperty;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;
import kiss.Managed;

@Icy(setterModifier = "final")
abstract class ScenarioBaseModel implements Directional, Profitable {

    /** The list entry orders. */
    @VisibleForTesting
    @Managed
    Deque<Order> entries = new ArrayDeque();

    /** The list exit orders. */
    @VisibleForTesting
    @Managed
    Deque<Order> exits = new ArrayDeque();

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        throw new Error("This method never will be called! Fix bug!");
    }

    /**
     * A total size of entry orders.
     * 
     * @return A total size of entry orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num entrySize() {
        return Num.ZERO;
    }

    /**
     * A total size of executed entry orders.
     * 
     * @return A total size of executed entry orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num entryExecutedSize() {
        return Num.ZERO;
    }

    /**
     * An average price of executed entry orders.
     * 
     * @return An average price of executed entry orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num entryPrice() {
        return Num.ZERO;
    }

    /**
     * The total of all entry orders.
     * 
     * @return The total of all entry orders.
     */
    @Icy.Property(setterModifier = "final")
    public Num entryCommission() {
        return Num.ZERO;
    }

    /**
     * A total size of exit orders.
     * 
     * @return A total size of exit orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num exitSize() {
        return Num.ZERO;
    }

    /**
     * A total size of executed exit orders.
     * 
     * @return A total size of executed exit orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num exitExecutedSize() {
        return Num.ZERO;
    }

    /**
     * An average price of executed exit orders.
     * 
     * @return An average price of executed exit orders.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num exitPrice() {
        return Num.ZERO;
    }

    /**
     * The total of all exit orders.
     * 
     * @return The total of all exit orders.
     */
    @Icy.Property(setterModifier = "final")
    public Num exitCommission() {
        return Num.ZERO;
    }

    /**
     * The remaining size to stop this scenario.
     * 
     * @return
     */
    public final Num remainingSize() {
        return entryExecutedSize().minus(exitExecutedSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num realizedProfit() {
        return Num.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Num unrealizedProfit(Num currentPrice) {
        return currentPrice.diff(direction(), entryPrice()).multiply(entryExecutedSize().minus(exitExecutedSize())).minus(commission());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num commission() {
        return entryCommission().plus(exitCommission());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode(); // use Object#hashCode
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); // use Object#equals
    }
}