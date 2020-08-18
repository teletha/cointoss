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

public class PriceRangedVolume {

    /** The starting time of period. (epoch second) */
    public final long startTime;

    private final int priceBase;

    private final int priceRange;

    private final int scale;

    private final int tens;

    private final DoubleArray upper = new DoubleArray();

    private final DoubleArray lower = new DoubleArray();

    PriceRangedVolume(long startTime, Num priceBase, Num priceRange, int scale) {
        this.startTime = startTime;
        this.scale = scale;
        this.tens = (int) Math.pow(10, scale);
        this.priceBase = (int) Math.round(priceBase.doubleValue() * tens);
        this.priceRange = (int) Math.round(priceRange.doubleValue() * tens);
    }

    void update(Num price, double size) {
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
     * Compute the grouped price-ranged-volume data.
     * 
     * @param groupSize
     * @return
     */
    public GroupedVolumes grouped(int groupSize) {
        int size = (upper.size() + lower.size()) / groupSize + 1;
        DoubleArray prices = new DoubleArray(size);
        DoubleArray volumes = new DoubleArray(size);
        double max = 0;

        int now = 0;
        double volume = 0;
        for (int i = 0, end = lower.size(); i < end; i++) {
            volume += lower.get(i);

            if (++now == groupSize) {
                prices.add((priceBase - (i + 1) * priceRange) / tens);
                volumes.add(volume);
                max = Math.max(max, volume);
                volume = 0;
                now = 0;
            }
        }
        for (int i = 0, end = upper.size(); i < end; i++) {
            volume += upper.get(i);

            if (++now == groupSize) {
                prices.add((priceBase + i * priceRange) / tens);
                volumes.add(volume);
                max = Math.max(max, volume);
                volume = 0;
                now = 0;
            }
        }

        return new GroupedVolumes(startTime, max, prices, volumes);
    }

    /**
     * 
     */
    public static class GroupedVolumes {

        /** The starting time of period. (epoch second) */
        public final long startTime;

        /** The max volume in this period. */
        public final double maxVolume;

        /** The price list. */
        public final DoubleArray prices;

        /** The volume list. */
        public final DoubleArray volumes;

        private GroupedVolumes(long startTime, double maxVolume, DoubleArray prices, DoubleArray volumes) {
            this.startTime = startTime;
            this.maxVolume = maxVolume;
            this.prices = prices;
            this.volumes = volumes;
        }
    }
}
