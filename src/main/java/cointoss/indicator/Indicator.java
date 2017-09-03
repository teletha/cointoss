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

import java.io.Serializable;

import cointoss.TimeSeries;

/**
 * Indicator over a {@link TimeSeries time series}.
 * <p>
 * For each index of the time series, returns a value of type <b>T</b>.
 * 
 * @version 2017/08/25 22:16:06
 */
public interface Indicator<T> extends Serializable {

    /**
     * @param index the tick index
     * @return the value of the indicator
     */
    T getValue(int index);

    /**
     * Get the current tick.
     * 
     * @return
     */
    default T getCurrent() {
        return getValue(getTimeSeries().size() - 1);
    }

    /**
     * @return the related time series
     */
    TimeSeries getTimeSeries();
}
