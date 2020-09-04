/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import org.apache.logging.log4j.util.PerformanceSensitive;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.ObservableNumProperty;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;

@Icy(setterModifier = "final")
abstract class ScenarioBaseModel implements Directional, Profitable {

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
     * {@inheritDoc}
     */
    @Override
    @PerformanceSensitive
    public final Num profit(Num currentPrice) {
        return realizedProfit().plus(unrealizedProfit(currentPrice));
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
    @PerformanceSensitive
    public final Num unrealizedProfit(Num currentPrice) {
        return currentPrice.diff(direction(), entryPrice()).multiply(entryExecutedSize().minus(exitExecutedSize()));
    }
}