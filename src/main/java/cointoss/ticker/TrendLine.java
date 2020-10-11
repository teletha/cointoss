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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import cointoss.util.ring.RingBuffer;

public class TrendLine {

    /** The size of data. */
    private final int size;

    /** The actual data holder. */
    private final RingBuffer<double[]> data;

    private int currentInterval = 0;

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
    public DoubleUnaryOperator add(double x, double y) {
        data.add(new double[] {x, y});

        if (++currentInterval != size) {
            return null;
        }
        currentInterval = 0;
        return build();
    }

    public DoubleUnaryOperator build() {
        LinkedList<double[]> base = data.to(new LinkedList());

        while (2 < base.size()) {
            ListIterator<double[]> iterator = base.listIterator();

            while (iterator.hasNext()) {
                double[] first = iterator.next();

                if (iterator.hasNext()) {
                    double[] second = iterator.next();
                    if (first[1] < second[1]) {
                        iterator.previous();
                        iterator.previous();
                        iterator.remove();
                        iterator.next();
                    } else {
                        iterator.remove();
                    }
                }
            }
        }

        SimpleRegression simple = new SimpleRegression(true);
        for (double[] point : base) {
            simple.addData(point[0], point[1]);
        }
        return simple::predict;
    }
}
