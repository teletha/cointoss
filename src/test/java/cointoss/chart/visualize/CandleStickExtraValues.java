/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart.visualize;

/**
 * @version 2017/09/25 17:32:11
 */
public class CandleStickExtraValues {
    private final double close;

    private final double high;

    private final double low;

    private final double average;

    public CandleStickExtraValues(double close, double high, double low, double average) {
        this.close = close;
        this.high = high;
        this.low = low;
        this.average = average;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getAverage() {
        return average;
    }

    @Override
    public String toString() {
        return "CandleStickExtraValues{" + "close=" + close + ", high=" + high + ", low=" + low + ", average=" + average + '}';
    }
}
