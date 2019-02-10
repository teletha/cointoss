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
 * @version 2017/08/30 20:45:02
 */
/**
 * @version 2017/09/04 14:13:21
 */
public class AmountSummary {

    /** MAX value. */
    public Num min = Num.ZERO;

    /** MIN value. */
    public Num max = Num.ZERO;

    /** Total value. */
    public Num total = Num.ZERO;

    /** Number of values. */
    public int size = 0;

    /** Number of positive values. */
    int positive = 0;

    /**
     * Calculate mean.
     * 
     * @return
     */
    public Num mean() {
        return total.divide(Math.max(size, 1));
    }

    /**
     * Add new value to summarize.
     * 
     * @param value
     */
    void add(Num value) {
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
