/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;

import cointoss.chart.Chart;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;

/**
 * @version 2017/09/07 16:25:01
 */
public class ChartTest {

    private ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    @Test
    public void chart() throws Exception {
        Chart chart = new Chart(Duration.ofMinutes(1), null);
        chart.tick(create(0, 1, 1));
        chart.tick(create(50, 2, 1));
        chart.tick(create(70, 3, 1));
        chart.tick(create(90, 4, 1));

        assert chart.getTickCount() == 2;
        Tick tick = chart.getLastTick();
        assert tick.getOpenPrice().is(3);
        assert tick.getClosePrice().is(4);
        assert tick.getMaxPrice().is(4);
        assert tick.getMinPrice().is(3);
        assert tick.getVolume().is(2);

        chart.tick(create(120, 5, 1));
        assert chart.getTickCount() == 3;
        tick = chart.getLastTick();
        assert tick.getOpenPrice().is(5);
        assert tick.getClosePrice().is(5);
        assert tick.getMaxPrice().is(5);
        assert tick.getMinPrice().is(5);
        assert tick.getVolume().is(1);
    }

    @Test
    public void subchart() throws Exception {
        Chart min2 = new Chart(Duration.ofMinutes(2), null);
        Chart min1 = new Chart(Duration.ofMinutes(1), min2);
        min1.tick(create(0, 1, 1));
        min1.tick(create(80, 2, 1));
        min1.tick(create(100, 3, 1));

        assert min1.getTickCount() == 2;
        assert min2.getTickCount() == 1;

        Tick tick1 = min1.getLastTick();
        assert tick1.getOpenPrice().is(2);
        assert tick1.getClosePrice().is(3);
        assert tick1.getMaxPrice().is(3);
        assert tick1.getMinPrice().is(2);
        assert tick1.getVolume().is(2);

        Tick tick2 = min2.getLastTick();
        assert tick2.getOpenPrice().is(1);
        assert tick2.getClosePrice().is(3);
        assert tick2.getMaxPrice().is(3);
        assert tick2.getMinPrice().is(1);
        assert tick2.getVolume().is(3);
    }

    /**
     * Create executon.
     * 
     * @param time
     * @param price
     * @param size
     * @return
     */
    private Execution create(int time, int price, int size) {
        Execution e = new Execution();
        e.price = Decimal.valueOf(price);
        e.size = Decimal.valueOf(size);
        e.exec_date = base.plusSeconds(time);
        e.side = Side.BUY;

        return e;
    }
}
