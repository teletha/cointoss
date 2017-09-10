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

import cointoss.Execution;
import cointoss.Side;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 13:06:03
 */
abstract class ChartTestSupport {

    /** The starting time. */
    private final ZonedDateTime base = ZonedDateTime.now().withSecond(0).withNano(0);

    /**
     * Create chart with 1 minute duration.
     */
    protected final Chart createChart1sec(int... ticks) {
        Chart chart = new Chart(Duration.ofSeconds(1));

        for (int i = 0; i < ticks.length; i++) {
            chart.tick(execute(i, ticks[i], 1));
        }

        return chart;
    }

    /**
     * Create executon.
     * 
     * @param time
     * @param price
     * @param size
     * @return
     */
    private Execution execute(int time, int price, int size) {
        Execution e = new Execution();
        e.price = Decimal.valueOf(price);
        e.size = Decimal.valueOf(size);
        e.exec_date = base.plusSeconds(time);
        e.side = Side.BUY;

        return e;
    }
}
