/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import cointoss.Execution;
import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2018/07/03 18:09:20
 */
final class Totality {

    /** The latest price. */
    Num latestPrice = Num.ZERO;

    /** Total volume */
    Num longVolume = Num.ZERO;

    /** Total volume */
    Num longPriceIncrease = Num.ZERO;

    /** Total volume */
    Num shortVolume = Num.ZERO;

    /** Total volume */
    Num shortPriceDecrease = Num.ZERO;

    /**
     * Calculate total values by {@link Execution}.
     * 
     * @param execution The latest {@link Execution}.
     */
    void update(Execution execution) {
        Num price = execution.price;

        if (execution.side == Side.BUY) {
            longVolume = longVolume.plus(execution.size);
            longPriceIncrease = longPriceIncrease.plus(price.minus(latestPrice));
        } else {
            shortVolume = shortVolume.plus(execution.size);
            shortPriceDecrease = shortPriceDecrease.plus(latestPrice.minus(price));
        }

        // update at last
        latestPrice = price;
    }

    /**
     * Create immutable copy.
     * 
     * @return
     */
    Totality snapshot() {
        Totality snapshot = new Totality();
        snapshot.latestPrice = latestPrice;
        snapshot.longVolume = longVolume;
        snapshot.longPriceIncrease = longPriceIncrease;
        snapshot.shortVolume = shortVolume;
        snapshot.shortPriceDecrease = shortPriceDecrease;

        return snapshot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Totality [price=" + latestPrice + ", long=" + longVolume + "(" + longPriceIncrease + "), short=" + shortVolume + "(" + shortPriceDecrease + ")]";
    }

}