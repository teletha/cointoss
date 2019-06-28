/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Directional;
import cointoss.util.Num;
import cointoss.util.ObservableNumProperty;
import icy.manipulator.Icy;

@Icy(setterModifier = "final")
public abstract class EntryStatusModel implements Directional {

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
     * Calculate total profit or loss on the current price. From the point of performance view,
     * profit is not calculated realtimely.
     * 
     * @param currentPrice A current price.
     * @return A total profit or loss of this entry.
     */
    public final Num profit(Num currentPrice) {
        return realizedProfit().plus(unrealizedProfit(currentPrice));
    }

    /**
     * A realized profit or loss of this entry.
     * 
     * @return A realized profit or loss of this entry.
     */
    @Icy.Property(custom = ObservableNumProperty.class)
    public Num realizedProfit() {
        return Num.ZERO;
    }

    /**
     * Calculate unrealized profit or loss on the current price. From the point of performance view,
     * unrealized profit is not calculated realtimely.
     * 
     * @param currentPrice A current price.
     * @return An unrealized profit or loss of this entry.
     */
    public final Num unrealizedProfit(Num currentPrice) {
        return currentPrice.diff(direction(), entryPrice()).multiply(entryExecutedSize().minus(exitExecutedSize()));
    }
}
