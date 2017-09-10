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
    private final RingBuffer<T> cache = new RingBuffer(60 * 24);

    /** The target chart. */
    protected final Chart chart;

    /**
     * @param chart
     */
    protected Indicator(Chart chart) {
        this.chart = chart;
    }

    /**
     * Return the latest value.
     * 
     * @param offset
     * @return
     */
    public final T latest() {
        return latest(0);
    }

    /**
     * Return the latest indexed value.
     * 
     * @param offset
     * @return
     */
    public final T latest(int offset) {
        if (offset == 0) {
            return calculate(offset);
        } else {
            return cache.latest(offset, this::calculate);
        }
    }

    /**
     * Calculate the latest indexed value.
     * 
     * @param offset
     * @return
     */
    public abstract T calculate(int offset);
}
