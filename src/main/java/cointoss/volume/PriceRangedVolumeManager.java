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

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.ticker.Tick;
import cointoss.util.arithmetic.Num;
import cointoss.util.array.FloatList;
import cointoss.util.map.ConcurrentNavigableLongMap;
import cointoss.util.map.LongMap;
import kiss.I;
import kiss.Signal;

public class PriceRangedVolumeManager {

    /** The time-based cache. */
    private final ConcurrentNavigableLongMap<PriceRangedVolumePeriod[]> volumes = LongMap.createReversedMap();

    /** The volume for buyers. (latest session) */
    private PriceRangedVolumePeriod buyer;

    /** The volume for sellers. (latest session) */
    private PriceRangedVolumePeriod seller;

    private final int scale;

    private final int tens;

    /** The minimum price range. */
    private final int priceRange;

    /**
     * 
     */
    public PriceRangedVolumeManager(Num priceRange) {
        this.scale = Math.max(0, priceRange.scale());
        this.tens = (int) Math.pow(10, scale);
        this.priceRange = Math.round(priceRange.floatValue() * tens);
    }

    /**
     * End the current record and start a new one.
     * 
     * @param tick A starting point.
     */
    public void start(Tick tick) {
        start(tick.openTime, tick.openPrice);
    }

    /**
     * End the current record and start a new one.
     * 
     * @param startTime A starting time.
     * @param startPrice A starting price.
     */
    public void start(long startTime, double startPrice) {
        buyer = new PriceRangedVolumePeriod(startTime, startPrice);
        seller = new PriceRangedVolumePeriod(startTime, startPrice);
        volumes.put(startTime, new PriceRangedVolumePeriod[] {buyer, seller});
    }

    /**
     * Update the current record.
     * 
     * @param e
     */
    public void add(Execution e) {
        if (e.direction == Direction.BUY) {
            buyer.update(e.price, e.size.floatValue());
        } else {
            seller.update(e.price, e.size.floatValue());
        }
    }

    /**
     * Update the current record.
     * 
     * @param e
     */
    public void update(Execution e) {
        if (e.direction == Direction.BUY) {
            buyer.update(e.price, e.size.floatValue());
        } else {
            seller.update(e.price, e.size.floatValue());
        }
    }

    /**
     * Retrieve the latest record.
     * 
     * @return
     */
    public PriceRangedVolumePeriod[] latest() {
        return new PriceRangedVolumePeriod[] {buyer, seller};
    }

    /**
     * Retrieve all past records (without the latest).
     * 
     * @return
     */
    public Signal<PriceRangedVolumePeriod[]> past() {
        return I.signal(volumes.values()).skip(1);
    }

    /**
     * Expose for test.
     * 
     * @param startTime
     * @param startPrice
     * @return
     */
    PriceRangedVolumePeriod createPeriod(long startTime, double startPrice) {
        return new PriceRangedVolumePeriod(startTime, startPrice);
    }

    public class PriceRangedVolumePeriod {

        /** The starting time of this period. (epoch second) */
        public final long startTime;

        /** The integral starting price of this period. */
        private final int startPrice;

        private final FloatList upper = new FloatList();

        private final FloatList lower = new FloatList();

        private PriceRangedVolumePeriod(long startTime, double startPrice) {
            this.startTime = startTime;
            this.startPrice = Math.round((float) startPrice * tens);
        }

        /**
         * Update volume by price.
         * 
         * @param price A target price.
         * @param volume A target volume.
         */
        void update(Num price, float volume) {
            int diff = price.decuple(scale).intValue() - startPrice;

            if (0 <= diff) {
                upper.increment(diff / priceRange, volume);
            } else {
                // Convert a and b to a double, and you can use the division and Math.ceil as you
                // wanted
                // it to work. However I strongly discourage the use of this approach, because
                // double
                // division can be imprecise and slow.
                // int offset = (int) Math.ceil(a / b) - 1;
                //
                // This is very short, but maybe for some less intuitive. I think this less
                // intuitive approach would be faster than the double division.
                // Please note that this doesn't work for b < 0.
                lower.increment((-diff + priceRange - 1) / priceRange - 1, volume);
            }
        }

        /**
         * Get price ranged volume by price.
         * 
         * @param price A target price (NOT price range). It is round to the suitable price range
         *            automatically.
         * @return A price ranged valume.
         */
        float volumeAt(float price) {
            int diff = (int) (price * tens) - startPrice;

            if (0 <= diff) {
                return upper.get(diff / priceRange);
            } else {
                // Convert a and b to a double, and you can use the division and Math.ceil as you
                // wanted
                // it to work. However I strongly discourage the use of this approach, because
                // double
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