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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cointoss.Execution;
import cointoss.market.bitflyer.BitFlyer;

/**
 * @version 2018/01/29 10:01:00
 */
public class TickerTest {

    private Ticker ticker;

    private ZonedDateTime start = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, Execution.UTC);

    private ZonedDateTime minite1 = start.plus(1, ChronoUnit.MINUTES);

    private ZonedDateTime hour12 = start.plus(12, ChronoUnit.HOURS);

    @Before
    public void setup() {
        ticker = new Ticker(BitFlyer.FX_BTC_JPY.log());
    }

    @Test
    public void second5() throws Exception {
        List<Tick> ticks = ticker.read(start, minite1, TickSpan.Second5, true).diff().toList();
        assert ticks.size() == 12;
    }

    @Test
    public void second10() throws Exception {
        List<Tick> ticks = ticker.read(start, minite1, TickSpan.Second10, true).diff().toList();
        assert ticks.size() == 6;
    }

    @Test
    public void second30() throws Exception {
        List<Tick> ticks = ticker.read(start, minite1, TickSpan.Second30, true).diff().toList();
        assert ticks.size() == 2;
    }

    @Test
    public void hour() throws Exception {
        List<Tick> ticks = ticker.read(start, hour12, TickSpan.Minute10, true).diff().toList();
        assert ticks.size() == 72;
    }
}
