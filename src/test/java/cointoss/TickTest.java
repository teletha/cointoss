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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import cointoss.Amount;
import cointoss.Execution;
import cointoss.Side;
import cointoss.TimeSeries;

/**
 * @version 2017/08/23 16:25:54
 */
public class TickTest {

    @Test
    public void tick() throws Exception {
        TimeSeries ticks = new TimeSeries();
        ticks.tick(tick("00:00:01", Side.BUY, 1, 20));

        assert ticks.latest().opening.is(20);
        assert ticks.latest().closing.is(20);
        assert ticks.latest().highest.is(20);
        assert ticks.latest().lowest.is(20);
        assert ticks.latest().volume.is(1);
        assert ticks.latest().volumeBuy.is(1);
        assert ticks.latest().volumeSell.is(0);
        assert ticks.latest().startTime.isEqual(time("00:00:00"));
        assert ticks.latest().endTime.isEqual(time("00:01:00"));
    }

    @Test
    public void tickWithMultipleExecutions() throws Exception {
        TimeSeries ticks = new TimeSeries();
        ticks.tick(tick("00:01:10", Side.BUY, 1, 20));
        ticks.tick(tick("00:01:20", Side.SELL, 1, 10));
        ticks.tick(tick("00:01:30", Side.SELL, 1, 30));
        ticks.tick(tick("00:01:40", Side.BUY, 1, 40));
        ticks.tick(tick("00:01:50", Side.BUY, 1, 20));

        assert ticks.latest().opening.is(20);
        assert ticks.latest().closing.is(20);
        assert ticks.latest().highest.is(40);
        assert ticks.latest().lowest.is(10);
        assert ticks.latest().volume.is(5);
        assert ticks.latest().volumeBuy.is(3);
        assert ticks.latest().volumeSell.is(2);
        assert ticks.latest().startTime.isEqual(time("00:01:00"));
        assert ticks.latest().endTime.isEqual(time("00:02:00"));
    }

    @Test
    public void ticks() throws Exception {
        TimeSeries ticks = new TimeSeries();
        ticks.tick(tick("00:01:10", Side.BUY, 1, 20));
        ticks.tick(tick("00:02:20", Side.SELL, 1, 10));
        ticks.tick(tick("00:03:30", Side.SELL, 1, 30));
        ticks.tick(tick("00:04:40", Side.BUY, 1, 40));
        ticks.tick(tick("00:05:50", Side.BUY, 1, 20));

        assert ticks.latest().opening.is(20);
        assert ticks.latest().closing.is(20);
        assert ticks.latest().highest.is(20);
        assert ticks.latest().lowest.is(20);
        assert ticks.latest().volume.is(1);
        assert ticks.latest().volumeBuy.is(1);
        assert ticks.latest().volumeSell.is(0);
        assert ticks.latest().startTime.isEqual(time("00:05:00"));
        assert ticks.latest().endTime.isEqual(time("00:06:00"));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param time
     * @param side
     * @param size
     * @param price
     * @return
     */
    private Execution tick(String time, Side side, int size, int price) {
        Execution e = new Execution();
        e.side = side;
        e.size = Amount.of(size);
        e.price = Amount.of(price);
        e.exec_date = LocalDate.now().atTime(LocalTime.parse(time, format));

        return e;
    }

    private LocalDateTime time(String time) {
        return LocalDate.now().atTime(LocalTime.parse(time, format));
    }

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
}
