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
 * @version 2017/09/10 12:58:15
 */
public class PriceIndicatorTest extends ChartTestSupport {

    @Test
    public void close() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.close(chart);
        assert indicator.get(4).is(5);
        assert indicator.get(3).is(4);
        assert indicator.get(2).is(3);
        assert indicator.get(1).is(2);
        assert indicator.get(0).is(1);
    }

    @Test
    public void open() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.open(chart);
        assert indicator.get(4).is(4);
        assert indicator.get(3).is(3);
        assert indicator.get(2).is(2);
        assert indicator.get(1).is(1);
        assert indicator.get(0).is(1);
    }

    @Test
    public void max() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.max(chart);
        assert indicator.get(4).is(5);
        assert indicator.get(3).is(4);
        assert indicator.get(2).is(3);
        assert indicator.get(1).is(2);
        assert indicator.get(0).is(1);
    }

    @Test
    public void min() throws Exception {
        Chart chart = createChart1sec(1, 2, 3, 4, 5);
        PriceIndicator indicator = PriceIndicator.min(chart);
        assert indicator.get(4).is(5);
        assert indicator.get(3).is(4);
        assert indicator.get(2).is(3);
        assert indicator.get(1).is(2);
        assert indicator.get(0).is(1);
    }
}
