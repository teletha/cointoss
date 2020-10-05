/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.Arrays;

import cointoss.util.ring.RingBuffer;

public class TrendLine {

    /** The size of data. */
    private final int size;

    /** The actual data holder. */
    private final RingBuffer<double[]> data;

    /**
     * Create.
     * 
     * @param dataSize
     */
    public TrendLine(int dataSize) {
        if (dataSize < 10) {
            throw new IllegalArgumentException("Data size must be greater than 10. ");
        }
        this.size = dataSize;
        this.data = new RingBuffer(dataSize);
    }

    /**
     * Add data.
     * 
     * @param x
     * @param y
     */
    public void add(double x, double y) {
        data.add(new double[] {x, y});
    }

    public void build() {
        double[][] base = data.toArray();

        while (2 < base.length) {
            int filteredSize = base.length % 4 == 0 ? base.length / 2 : base.length / 2 + 1;
            double[][] filtered = new double[filteredSize][];
            for (int i = 0; i < filteredSize; i++) {
                filtered[i] = base[i + 1] != null && base[i][1] < base[i + 1][1] ? base[i + 1] : base[i];
            }
            base = filtered;
        }
        System.out.println(Arrays.toString(base));
    }
}
