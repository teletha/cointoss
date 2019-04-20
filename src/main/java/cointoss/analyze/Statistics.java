/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import cointoss.util.Num;

/**
 * This class provides a means of updating summary statistics as each new data point is added. The
 * data points are not stored, and values are updated with online algorithm.
 */
public class Statistics {

    /** MAX value. */
    private Num min = Num.ZERO;

    /** MIN value. */
    private Num max = Num.ZERO;

    /** Total value. */
    private Num total = Num.ZERO;

    /** Number of values. */
    private int size = 0;

    /** Mean value. */
    private Num mean = Num.ZERO;

    /** Temporary values to calculate variance. */
    private Num m2 = Num.ZERO, m3 = Num.ZERO, m4 = Num.ZERO;

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public Statistics add(long value) {
        return add(Num.of(value));
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public Statistics add(double value) {
        return add(Num.of(value));
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public Statistics add(Num value) {
        size++;
        min = min.isZero() ? value : Num.min(min, value);
        max = max.isZero() ? value : Num.max(max, value);
        total = total.plus(value);
    
        Num delta = value.minus(mean);
        Num deltaN = delta.divide(size);
        Num deltaN2 = deltaN.pow(2);
        Num term = delta.multiply(deltaN).multiply(size - 1);
    
        mean = mean.plus(deltaN);
        m4 = m4.plus(term.multiply(deltaN2).multiply(size * size - 3 * size + 3).plus(deltaN2.multiply(m2).multiply(6)))
                .minus(deltaN.multiply(m3).multiply(4));
        m3 = m3.plus(term.multiply(deltaN).multiply(size - 2).minus(deltaN.multiply(m2).multiply(3)));
        m2 = m2.plus(delta.multiply(value.minus(mean)));
    
        return this;
    }

    /**
     * Calculate kurtosis value.
     * 
     * @return A kurtosis value.
     */
    public Num kurtosis() {
        return m2.isZero() ? Num.ZERO : m4.multiply(size).divide(m2.pow(2)).minus(3);
    }

    /**
     * Calculate maximum value.
     * 
     * @return A maximum value.
     */
    public Num max() {
        return max;
    }

    /**
     * Calculate minimum value.
     * 
     * @return A minimum value.
     */
    public Num min() {
        return min;
    }

    /**
     * Calculate mean value.
     * 
     * @return A mean value.
     */
    public Num mean() {
        return mean;
    }

    /**
     * Calculate skweness value.
     * 
     * @return A sckewness value.
     */
    public Num skewness() {
        Num divide = m3.multiply(Math.sqrt(size));

        return divide.isZero() ? Num.ZERO : divide.divide(m2.pow(1.5));
    }

    /**
     * The number of items.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Calculate standard deviation value.
     * 
     * @return A standard deviation value.
     */
    public Num standardDeviation() {
        return variance().sqrt();
    }

    /**
     * Calculate total value.
     * 
     * @return A total value.
     */
    public Num total() {
        return total;
    }

    /**
     * Calculate variance value.
     * 
     * @return A variance value.
     */
    public Num variance() {
        return m2.divide(size + 1e-15);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append("最小")
                .append(min.asJPY(7))
                .append("\t最大")
                .append(max.asJPY(7))
                .append("\t平均")
                .append(mean().asJPY(7))
                .append("\t合計")
                .append(total.asJPY(12))
                .toString();
    }
}
