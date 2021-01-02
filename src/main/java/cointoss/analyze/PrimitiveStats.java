/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

import java.util.function.DoubleFunction;

import cointoss.util.Primitives;

/**
 * This class provides a means of updating summary statistics as each new data point is added. The
 * data points are not stored, and values are updated with online algorithm.
 */
public class PrimitiveStats {

    /** MAX value. */
    private double min = 0;

    /** MIN value. */
    private double max = 0;

    /** Total value. */
    private double total = 0;

    /** Number of values. */
    private int size = 0;

    /** Number of values. */
    private double decayedSize = 0;

    /** Mean value. */
    private double mean = 0;

    /** Temporary values to calculate variance. */
    private double m2 = 0, m3 = 0, m4 = 0;

    /** The value formatter. */
    private DoubleFunction<String> formatter = Primitives.DecimalScale2::format;

    private boolean negative = false;

    private double decayFactor = 1;

    /**
     * Set the decay factor.
     * 
     * @param factor
     * @return Chainable API.
     */
    public PrimitiveStats decay(double factor) {
        if (Primitives.within(0, factor, 1)) {
            this.decayFactor = factor;
        }
        return this;
    }

    /**
     * Set value formatter.
     * 
     * @param formatter A value formatter.
     * @return Chainable API.
     */
    public PrimitiveStats formatter(DoubleFunction<String> formatter) {
        if (formatter != null) {
            this.formatter = formatter;
        }
        return this;
    }

    /**
     * Set this {@link PrimitiveStats} deal with negative values.
     * 
     * @return Chainable API.
     */
    public PrimitiveStats negative() {
        this.negative = true;
        return this;
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public PrimitiveStats add(long value) {
        return add((double) value);
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public PrimitiveStats add(double value) {
        if (decayFactor != 1d) {
            decayedSize = decayFactor * decayedSize;
            total = decayFactor * total;
            mean = decayFactor * mean;
            m2 = decayFactor * m2;
            m3 = decayFactor * m3;
            m4 = decayFactor * m4;
        }

        size++;
        decayedSize = decayedSize + 1d;
        min = min == 0d ? value : negative ? Math.max(min, value) : Math.min(min, value);
        max = max == 0d ? value : negative ? Math.min(max, value) : Math.max(max, value);
        total = total + value;

        double delta = value - mean;
        double deltaN = delta / decayedSize;
        double deltaN2 = Math.pow(deltaN, 2);
        double term = delta * deltaN * (decayedSize - 1);

        mean = mean + deltaN;
        m4 = m4 + (term * deltaN2 * (decayedSize * decayedSize - (3 * decayedSize) + 3) + (deltaN2 * m2 * 6)) - (deltaN * m3 * 4);
        m3 = m3 + (term * deltaN * (decayedSize - 2)) - (deltaN * m2 * 3);
        m2 = m2 + (delta * (value - mean));

        return this;
    }

    /**
     * Calculate kurtosis value.
     * 
     * @return A kurtosis value.
     */
    public double kurtosis() {

        return m2 == 0d ? 0d : (m4 * decayedSize / Math.pow(m2, 2)) - 3;
    }

    /**
     * Calculate maximum value.
     * 
     * @return A maximum value.
     */
    public double max() {
        return max;
    }

    /**
     * Calculate maximum value.
     * 
     * @return A formatted maximum value.
     */
    public String formattedMax() {
        return formatter.apply(max);
    }

    /**
     * Calculate minimum value.
     * 
     * @return A minimum value.
     */
    public double min() {
        return min;
    }

    /**
     * Calculate minimum value.
     * 
     * @return A formatted minimum value.
     */
    public String formattedMin() {
        return formatter.apply(min);
    }

    /**
     * Calculate mean value.
     * 
     * @return A mean value.
     */
    public double mean() {
        return mean;
    }

    /**
     * Calculate mean value.
     * 
     * @return A formatted mean value.
     */
    public String formattedMean() {
        return formatter.apply(mean);
    }

    /**
     * Calculate skweness value.
     * 
     * @return A sckewness value.
     */
    public double skewness() {
        double divide = m3 * Math.sqrt(decayedSize);

        return divide == 0d ? 0 : divide / Math.pow(m2, 1.5);
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
     * The approximate value of items.
     * 
     * @return
     */
    public int decayedSize() {
        return (int) decayedSize;
    }

    /**
     * Calculate standard deviation value.
     * 
     * @return A standard deviation value.
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * Calculate total value.
     * 
     * @return A total value.
     */
    public double total() {
        return total;
    }

    /**
     * Calculate total value.
     * 
     * @return A formatted total value.
     */
    public String formattedTotal() {
        return formatter.apply(total);
    }

    /**
     * Calculate variance value.
     * 
     * @return A variance value.
     */
    public double variance() {
        return m2 / decayedSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append("最小")
                .append(padding(formatter.apply(min)))
                .append("\t最大")
                .append(padding(formatter.apply(max)))
                .append("\t平均")
                .append(padding(formatter.apply(mean)))
                .append("\t合計")
                .append(padding(formatter.apply(total)))
                .toString();
    }

    /**
     * Padding left.
     * 
     * @param value
     * @return
     */
    private String padding(String value) {
        int length = value.length();

        if (4 < length) {
            return value;
        }

        StringBuilder builder = new StringBuilder(value);
        for (int i = length; i < 4; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }
}