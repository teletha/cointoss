/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package eu.verdelhan.ta4j;

import java.io.Serializable;

/**
 * Indicator over a {@link TimeSeries time series}.
 * <p>
 * For each index of the time series, returns a value of type <b>T</b>.
 * 
 * @param <T> the type of returned value (Double, Boolean, etc.)
 */
public interface Indicator<T> extends Serializable {

    /**
     * @param index the tick index
     * @return the value of the indicator
     */
    T getValue(int index);

    /**
     * @return the related time series
     */
    TimeSeries getTimeSeries();

    /**
     * Retrieve the latest value.
     * 
     * @return
     */
    default T getEndValue() {
        return getValue(getTimeSeries().getEndIndex());
    }
}
