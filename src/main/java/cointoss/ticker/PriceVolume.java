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

import java.util.Arrays;

import cointoss.util.DoubleArray;
import cointoss.util.Num;

public class PriceVolume {

    private final int priceBase;

    private final int priceRange;

    private final int scale;

    private final int tens;

    private final DoubleArray upper = new DoubleArray();

    private final DoubleArray lower = new DoubleArray();

    /**
     * @param priceBase
     */
    public PriceVolume(Num priceBase, Num priceRange, int scale) {
        this.scale = scale;
        this.tens = (int) Math.pow(10, scale);
        this.priceBase = (int) Math.round(priceBase.doubleValue() * tens);
        this.priceRange = (int) Math.round(priceRange.doubleValue() * tens);
    }

    /**
     * @param price
     * @param size
     */
    public void update(Num price, double size) {
        int diff = price.decuple(scale).intValue() - priceBase;

        if (0 <= diff) {
            upper.increment(diff / priceRange, size);
        } else {
            // Convert a and b to a double, and you can use the division and Math.ceil as you wanted
            // it to work. However I strongly discourage the use of this approach, because double
            // division can be imprecise and slow.
            // int offset = (int) Math.ceil(a / b) - 1;
            //
            // This is very short, but maybe for some less intuitive. I think this less
            // intuitive approach would be faster than the double division.
            // Please note that this doesn't work for b < 0.
            lower.increment((-diff + priceRange - 1) / priceRange - 1, size);
        }
    }

    public double volumeAt(double price) {
        int diff = (int) (price * tens) - priceBase;

        if (0 <= diff) {
            return upper.get(diff / priceRange);
        } else {
            // Convert a and b to a double, and you can use the division and Math.ceil as you wanted
            // it to work. However I strongly discourage the use of this approach, because double
            // division can be imprecise and slow.
            // int offset = (int) Math.ceil(a / b) - 1;
            //
            // This is very short, but maybe for some less intuitive. I think this less
            // intuitive approach would be faster than the double division.
            // Please note that this doesn't work for b < 0.
            return lower.get((-diff + priceRange - 1) / priceRange - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(Arrays.toString(upper.asArray()));
        b.append("\r\n");
        b.append(Arrays.toString(lower.asArray()));
        return b.toString();
    }
}
