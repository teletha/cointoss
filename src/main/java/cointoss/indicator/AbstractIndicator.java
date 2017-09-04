/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.indicator;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;

import cointoss.TimeSeries;

/**
 * @version 2017/08/25 22:25:10
 */
@SuppressWarnings("serial")
public abstract class AbstractIndicator<T> implements Indicator<T> {

    /** INTERNAL ACCESS */
    protected final TimeSeries series;

    private final MutableIntObjectMap<T> cache = IntObjectMaps.mutable.empty();

    /**
     * Constructor.
     * 
     * @param indicator
     */
    public AbstractIndicator(Indicator<T> indicator) {
        this(indicator.getTimeSeries());
    }

    /**
     * Constructor.
     * 
     * @param series the related time series
     */
    public AbstractIndicator(TimeSeries series) {
        this.series = series;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue(int index) {
        return cache.getIfAbsentPut(index, () -> calculate(index));
    }

    /**
     * Calculate the indexed value.
     * 
     * @param index
     * @return
     */
    protected abstract T calculate(int index);

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeSeries getTimeSeries() {
        return series;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
