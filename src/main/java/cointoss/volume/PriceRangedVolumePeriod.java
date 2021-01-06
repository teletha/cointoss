/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.volume;

import cointoss.util.arithmetic.Num;
import cointoss.util.array.FloatList;

public class PriceRangedVolumePeriod {

    /** The starting time of this period. (epoch second) */
    public final long startTime;

    /** The integral starting price of this period. */
    private final int startPrice;

    /** The integral price range. */
    private final int priceRange;

    private final int scale;

    private final int tens;

    private final FloatList upper = new FloatList();

    private final FloatList lower = new FloatList();

    private float max;

    PriceRangedVolumePeriod(long startTime, Num startPrice, Num priceRange) {
        this.startTime = startTime;
        this.scale = Math.max(0, priceRange.scale());
        this.tens = (int) Math.pow(10, scale);
        this.startPrice = Math.round(startPrice.floatValue() * tens);
        this.priceRange = Math.round(priceRange.floatValue() * tens);
    }

    /**
     * Update volume by price.
     * 
     * @param price A target price.
     * @param volume A target volume.
     */
    final void update(Num price, float volume) {
        int diff = price.decuple(scale).intValue() - startPrice;
        float updated;

        if (0 <= diff) {
            updated = upper.increment(diff / priceRange, volume);
        } else {
            // Convert a and b to a double, and you can use the division and Math.ceil as you wanted
            // it to work. However I strongly discourage the use of this approach, because double
            // division can be imprecise and slow.
            // int offset = (int) Math.ceil(a / b) - 1;
            //
            // This is very short, but maybe for some less intuitive. I think this less
            // intuitive approach would be faster than the double division.
            // Please note that this doesn't work for b < 0.
            updated = lower.increment((-diff + priceRange - 1) / priceRange - 1, volume);
        }

        if (max < updated) {
            max = updated;
        }
    }

    /**
     * Get the maximum volumes in this session.
     * 
     * @return
     */
    public float max() {
        return max;
    }

    /**
     * Get price ranged volume by price.
     * 
     * @param price A target price (NOT price range). It is round to the suitable price range
     *            automatically.
     * @return A price ranged valume.
     */
    public float volumeAt(float price) {
        int diff = (int) (price * tens) - startPrice;

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
    public GroupedVolumes aggregateBySize(int groupSize) {
        int size = (upper.size() + lower.size()) / groupSize + 1;
        FloatList prices = new FloatList(size);
        FloatList volumes = new FloatList(size);
        float max = 0;
        float half = priceRange / tens / 2;

        int now = 0;
        float volume = 0;
        for (int i = 0, end = lower.size(); i < end; i++) {
            volume += lower.get(i);

            if (++now == groupSize) {
                prices.add((startPrice - i * priceRange) / (float) tens - half);
                volumes.add(volume);
                max = Math.max(max, volume);
                volume = 0;
                now = 0;
            }
        }
        for (int i = 0, end = upper.size(); i < end; i++) {
            volume += upper.get(i);

            if (++now == groupSize) {
                prices.add((startPrice + i * priceRange) / (float) tens + half);
                volumes.add(volume);
                max = Math.max(max, volume);
                volume = 0;
                now = 0;
            }
        }
        return new GroupedVolumes(startTime, max, prices, volumes);
    }

    /**
     * Compute the grouped price-ranged-volume data.
     * 
     * @param range
     * @return
     */
    public GroupedVolumes aggregateByPrice(Num range) {
        return aggregateBySize(Math.max(1, range.multiply(tens).intValue() / priceRange));
    }

    /**
     * 
     */
    public static class GroupedVolumes {

        /** The starting time of period. (epoch second) */
        public final long startTime;

        /** The max volume in this period. */
        public final float maxVolume;

        /** The price list. */
        public final FloatList prices;

        /** The volume list. */
        public final FloatList volumes;

        private GroupedVolumes(long startTime, float maxVolume, FloatList prices, FloatList volumes) {
            this.startTime = startTime;
            this.maxVolume = maxVolume;
            this.prices = prices;
            this.volumes = volumes;
        }
    }
}