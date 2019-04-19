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

public class NumSummary {

    /** MAX value. */
    private Num min = Num.ZERO;

    /** MIN value. */
    private Num max = Num.ZERO;

    /** Total value. */
    private Num total = Num.ZERO;

    /** Number of values. */
    private int size = 0;

    /** Number of positive values. */
    private int positive = 0;

    /**
     * Calculate maximum.
     * 
     * @return A maximum.
     */
    public Num max() {
        return max;
    }

    /**
     * Calculate minimum.
     * 
     * @return A minimum.
     */
    public Num min() {
        return min;
    }

    /**
     * Calculate mean.
     * 
     * @return
     */
    public Num mean() {
        return total.divide(Math.max(size, 1));
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
     * Calculate total value.
     * 
     * @return A total value.
     */
    public Num total() {
        return total;
    }

    /**
     * Add new value to summarize.
     * 
     * @param value
     */
    public void add(Num value) {
        min = min.isZero() ? value : Num.min(min, value);
        max = max.isZero() ? value : Num.max(max, value);
        total = total.plus(value);
        size++;
        if (value.isPositive()) positive++;
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
