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

import org.junit.Test;

import cointoss.chart.simple.PriceIndicator;

/**
 * @version 2017/09/10 14:21:07
 */
public class IndicatorTest extends ChartTestSupport {

    @Test
    public void get() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.close(chart);
        assert indicator.get(4).is(5);
        assert indicator.get(3).is(4);
        assert indicator.get(2).is(3);
        assert indicator.get(1).is(2);
        assert indicator.get(0).is(1);
    }

    @Test
    public void getLast() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.close(chart);
        assert indicator.getLast(0).is(5);
        assert indicator.getLast(1).is(4);
        assert indicator.getLast(2).is(3);
        assert indicator.getLast(3).is(2);
        assert indicator.getLast(4).is(1);
    }

    @Test
    public void sma() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        Indicator indicator = PriceIndicator.close(chart).sma(10);
        assert indicator.get(0).is("1");
        assert indicator.get(1).is("1.5");
        assert indicator.get(2).is("2");
        assert indicator.get(3).is("2.5");
        assert indicator.get(4).is("3");
        assert indicator.get(5).is("3.5");
        assert indicator.get(6).is("4");
        assert indicator.get(7).is("4.5");
        assert indicator.get(8).is("5");
        assert indicator.get(9).is("5.5");
        assert indicator.get(10).is("6.5");
    }

    @Test
    public void ema() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Indicator indicator = PriceIndicator.close(chart).sma(5);
        assert indicator.get(0).is("1");
        assert indicator.get(1).is("1.5");
        assert indicator.get(2).is("2");
        assert indicator.get(3).is("2.5");
        assert indicator.get(4).is("3");
        assert indicator.get(5).is("4");
        assert indicator.get(6).is("5");
        assert indicator.get(7).is("6");
        assert indicator.get(8).is("7");
        assert indicator.get(9).is("8");
    }
}
