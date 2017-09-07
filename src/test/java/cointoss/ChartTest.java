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

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/07 16:25:01
 */
public class ChartTest {

    private ZonedDateTime base = ZonedDateTime.now();

    @Test
    public void chart() throws Exception {
        Chart chart = new Chart(Duration.ofSeconds(3), null);
        chart.tick(create(0, 1, 1));
        chart.tick(create(1, 2, 1));
        chart.tick(create(2, 3, 1));
        chart.tick(create(3, 4, 1));

        assert chart.getTickCount() == 1;
        assert chart.getLastTick().getOpenPrice().is(1);
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

        return e;
    }
}
