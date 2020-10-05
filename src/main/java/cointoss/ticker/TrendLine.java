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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<double[]> base = data.toList();

        while (2 < base.size()) {
            int filteredSize = base.size() / 2 + base.size() % 2;
            List<double[]> filtered = new ArrayList();
            for (int i = 0; i < filteredSize; i++) {
                double[] one = base.get(i * 2);
                if (base.size() == i * 2 + 1) {
                    filtered.add(one);
                } else {
                    double[] other = base.get(i * 2 + 1);
                    filtered.add(other != null && one[1] < other[1] ? other : one);
                }
            }
            base = filtered;
        }

        for (double[] item : base) {
            System.out.println(Arrays.toString(item));
        }
    }
}
