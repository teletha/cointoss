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
import cointoss.util.DoubleBiConsumer;
import cointoss.util.Num;

public class PriceRangedVolume {

    /** The starting time of period. (epoch seconds) */
    public final long startTime;

    private final int priceBase;

    private final int priceRange;

    private final int scale;

    private final int tens;

    private final DoubleArray upper = new DoubleArray();

    private final DoubleArray lower = new DoubleArray();

    public double max = 0;

    PriceRangedVolume(long startTime, Num priceBase, Num priceRange, int scale) {
        this.startTime = startTime;
        this.scale = scale;
        this.tens = (int) Math.pow(10, scale);
        this.priceBase = (int) Math.round(priceBase.doubleValue() * tens);
        this.priceRange = (int) Math.round(priceRange.doubleValue() * tens);
    }

    void update(Num price, double size) {
        double updated;
        int diff = price.decuple(scale).intValue() - priceBase;

        if (0 <= diff) {
            updated = upper.increment(diff / priceRange, size);
        } else {
            // Convert a and b to a double, and you can use the division and Math.ceil as you wanted
            // it to work. However I strongly discourage the use of this approach, because double
            // division can be imprecise and slow.
            // int offset = (int) Math.ceil(a / b) - 1;
            //
            // This is very short, but maybe for some less intuitive. I think this less
            // intuitive approach would be faster than the double division.
            // Please note that this doesn't work for b < 0.
            updated = lower.increment((-diff + priceRange - 1) / priceRange - 1, size);
        }

        if (max < updated) {
            max = updated;
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

    public void each(DoubleBiConsumer consumer) {
        for (int i = 0, size = lower.size(); i < size; i++) {
            consumer.accept((priceBase - i * priceRange) / tens, lower.get(i));
        }
        for (int i = 0, size = upper.size(); i < size; i++) {
            consumer.accept((priceBase + i * priceRange) / tens, upper.get(i));
        }
    }

    public void each(int group, DoubleBiConsumer consumer) {
        int now = 0;
        double totalSize = 0;
        for (int i = 0, size = lower.size(); i < size; i++) {
            totalSize += lower.get(i);

            if (++now == group) {
                consumer.accept((priceBase - (i + 1) * priceRange) / tens, totalSize);
                totalSize = 0;
                now = 0;
            }
        }
        for (int i = 0, size = upper.size(); i < size; i++) {
            totalSize += upper.get(i);

            if (++now == group) {
                consumer.accept((priceBase + i * priceRange) / tens, totalSize);
                totalSize = 0;
                now = 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(upper.size() + lower.size()).append(" ");
        b.append(Arrays.toString(upper.asArray()));
        b.append(" ");
        b.append(Arrays.toString(lower.asArray()));
        return b.toString();
    }

    private static class Freezed extends PriceRangedVolume {

    }

    private static class Realtime extends PriceRangedVolume {

    }
}
