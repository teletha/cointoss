/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

public class DoubleArray {

    /** The actual data holder. */
    private double[] values;

    /** The logical index. */
    private int logicalStart;

    /** The logical indx. */
    private int logicalEnd;

    /** The logical size. */
    private int logicalSize;

    /**
     * Create new array with capacity.
     * 
     * @param initialCapcity
     */
    public DoubleArray(int initialCapcity) {
        values = new double[initialCapcity];
    }

    /**
     * Add new value at last.
     * 
     * @param value
     */
    public void add(double value) {

    }

    public int size() {
        return logicalEnd - logicalStart;
    }

    public double[] array() {
        double[] copy = new double[size()];
        System.arraycopy(values, logicalStart, copy, 0, copy.length);
        return copy;
    }
}
