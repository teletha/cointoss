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

import java.util.function.Function;

import cointoss.util.arithmetic.Num;

/**
 * This class provides a means of updating summary statistics as each new data point is added. The
 * data points are not stored, and values are updated with online algorithm.
 */
public class NumStats {

    /** MAX value. */
    private Num min = Num.ZERO;

    /** MIN value. */
    private Num max = Num.ZERO;

    /** Total value. */
    private Num total = Num.ZERO;

    /** Number of values. */
    private int size = 0;

    /** Number of values. */
    private Num decayedSize = Num.ZERO;

    /** Mean value. */
    private Num mean = Num.ZERO;

    /** Temporary values to calculate variance. */
    private Num m2 = Num.ZERO, m3 = Num.ZERO, m4 = Num.ZERO;

    /** The value formatter. */
    private Function<Num, String> formatter = num -> num.toString();

    private boolean negative = false;

    private Num decayFactor = Num.ONE;

    /**
     * Set the decay factor.
     * 
     * @param factor
     * @return Chainable API.
     */
    public NumStats decay(Num factor) {
        if (Num.within(Num.ZERO, factor, Num.ONE)) {
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
    public NumStats formatter(Function<Num, String> formatter) {
        if (formatter != null) {
            this.formatter = formatter;
        }
        return this;
    }

    /**
     * Set this {@link NumStats} deal with negative values.
     * 
     * @return Chainable API.
     */
    public NumStats negative() {
        this.negative = true;
        return this;
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public NumStats add(long value) {
        return add(Num.of(value));
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public NumStats add(double value) {
        return add(Num.of(value));
    }

    /**
     * Add new value to summarize.
     * 
     * @param value A value to add.
     * @return Chainable API.
     */
    public NumStats add(Num value) {
        if (decayFactor.isNot(Num.ONE)) {
            decayedSize = decayFactor.multiply(decayedSize);
            total = decayFactor.multiply(total);
            mean = decayFactor.multiply(mean);
            m2 = decayFactor.multiply(m2);
            m3 = decayFactor.multiply(m3);
            m4 = decayFactor.multiply(m4);
        }

        size++;
        decayedSize = decayedSize.plus(Num.ONE);
        min = min.isZero() ? value : negative ? Num.max(min, value) : Num.min(min, value);
        max = max.isZero() ? value : negative ? Num.min(max, value) : Num.max(max, value);
        total = total.plus(value);

        Num delta = value.minus(mean);
        Num deltaN = delta.divide(decayedSize);
        Num deltaN2 = deltaN.pow(2);
        Num term = delta.multiply(deltaN).multiply(decayedSize.minus(Num.ONE));

        mean = mean.plus(deltaN);
        m4 = m4.plus(term.multiply(deltaN2)
                .multiply(decayedSize.multiply(decayedSize).minus(Num.THREE.multiply(decayedSize)).plus(Num.THREE))
                .plus(deltaN2.multiply(m2).multiply(6))).minus(deltaN.multiply(m3).multiply(4));
        m3 = m3.plus(term.multiply(deltaN).multiply(decayedSize.minus(Num.TWO)).minus(deltaN.multiply(m2).multiply(3)));
        m2 = m2.plus(delta.multiply(value.minus(mean)));

        return this;
    }

    /**
     * Calculate kurtosis value.
     * 
     * @return A kurtosis value.
     */
    public Num kurtosis() {
        return m2.isZero() ? Num.ZERO : m4.multiply(decayedSize).divide(m2.pow(2)).minus(3);
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
    public Num min() {
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
    public Num mean() {
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
    public Num skewness() {
        Num divide = m3.multiply(decayedSize.sqrt());

        return divide.isZero() ? Num.ZERO : divide.divide(m2.sqrt().pow(3));
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
        return decayedSize.intValue();
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
    public Num variance() {
        return m2.divide(decayedSize);
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