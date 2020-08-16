/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import cointoss.util.DoubleArray;
import cointoss.util.Num;

public class PriceVolume {

    private final double priceBase;

    private final double priceRange;

    private final DoubleArray upper = new DoubleArray();

    private final DoubleArray lower = new DoubleArray();

    /**
     * @param startingPrice
     */
    public PriceVolume(Num startingPrice, Num priceRange) {
        this.priceBase = startingPrice.doubleValue();
        this.priceRange = priceRange.doubleValue();
    }

    /**
     * 
     * @param price
     * @param size
     */
    public void update(Num price, double size) {
        double diff = price.doubleValue() - priceBase;

        if (0 <= diff) {
            int offset = (int) (diff / priceRange);
            upper.increment(offset, size);
        } else {
            int offset = (int) (-diff / priceRange);
            lower.increment(offset, size);
        }
    }

    public double volumeAt(double price) {
        double diff = price - priceBase;

        if (0 <= diff) {
            int offset = (int) (diff / priceRange);
            return upper.get(offset);
        } else {
            int offset = (int) (-diff / priceRange);
            return lower.get(offset);
        }
    }

}
