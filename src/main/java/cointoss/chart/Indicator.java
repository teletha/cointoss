/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import cointoss.util.RingBuffer;

/**
 * @version 2017/09/10 12:18:58
 */
public abstract class Indicator<T> {

    /** The internal cache. */
    private final RingBuffer<T> cache;

    /** The target chart. */
    protected final Chart chart;

    /**
     * @param chart
     */
    protected Indicator(Chart chart) {
        this.chart = chart;
        this.cache = new RingBuffer(chart.ticks);
    }

    /**
     * Return the indexed value.
     * 
     * @param index
     * @return
     */
    public final T get(int index) {
        int end = chart.ticks.end() - 1;

        if (index == end) {
            return calculate(end);
        } else {
            return cache.get(index, this::calculate);
        }
    }

    /**
     * Calculate the indexed value.
     * 
     * @param index
     * @return
     */
    public abstract T calculate(int index);
}
