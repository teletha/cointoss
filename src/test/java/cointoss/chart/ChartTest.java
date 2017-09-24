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

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;

import cointoss.Execution;
import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2017/09/10 12:35:05
 */
public class ChartTest {

    private ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    @Test
    public void chart() throws Exception {
        Chart chart = new Chart(Duration.ofMinutes(1));
        chart.tick(create(0, 1, 1));
        chart.tick(create(50, 2, 1));
        chart.tick(create(70, 3, 1));
        chart.tick(create(90, 4, 1));

        assert chart.getTickCount() == 2;
        Tick tick = chart.getLastTick();
        assert tick.openPrice.is(3);
        assert tick.closePrice.is(4);
        assert tick.maxPrice.is(4);
        assert tick.minPrice.is(3);
        assert tick.volume.is(2);

        chart.tick(create(120, 5, 1));
        assert chart.getTickCount() == 3;
        tick = chart.getLastTick();
        assert tick.openPrice.is(5);
        assert tick.closePrice.is(5);
        assert tick.maxPrice.is(5);
        assert tick.minPrice.is(5);
        assert tick.volume.is(1);
    }

    @Test
    public void subchart() throws Exception {
        Chart min2 = new Chart(Duration.ofMinutes(2));
        Chart min1 = new Chart(Duration.ofMinutes(1), min2);
        min1.tick(create(0, 1, 1));
        min1.tick(create(80, 2, 1));
        min1.tick(create(100, 3, 1));
        min1.tick(create(120, 4, 1));
        min1.tick(create(150, 5, 1));

        assert min1.getTickCount() == 3;
        assert min2.getTickCount() == 1;

        Tick tick1 = min1.getLastTick();
        assert tick1.openPrice.is(4);
        assert tick1.closePrice.is(5);
        assert tick1.maxPrice.is(5);
        assert tick1.minPrice.is(4);
        assert tick1.volume.is(2);

        Tick tick2 = min2.getLastTick();
        assert tick2.openPrice.is(1);
        assert tick2.closePrice.is(3);
        assert tick2.maxPrice.is(3);
        assert tick2.minPrice.is(1);
        assert tick2.volume.is(3);
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
        e.price = Num.of(price);
        e.size = Num.of(size);
        e.exec_date = base.plusSeconds(time);
        e.side = Side.BUY;

        return e;
    }
}
