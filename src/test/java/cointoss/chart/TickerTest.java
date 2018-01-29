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

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cointoss.Execution;
import cointoss.chart.Ticker;
import cointoss.chart.Tick;
import cointoss.chart.TickSpan;
import cointoss.market.bitflyer.BitFlyer;
import filer.Filer;

/**
 * @version 2018/01/29 10:01:00
 */
public class TickerTest {

    private Ticker ticker;

    private ZonedDateTime start = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, Execution.UTC);

    private ZonedDateTime minite1 = start.plus(1, ChronoUnit.MINUTES).plus(4, ChronoUnit.SECONDS);

    private ZonedDateTime minite3 = start.plus(3, ChronoUnit.MINUTES).plus(4, ChronoUnit.SECONDS);

    @Before
    public void setup() {
        Path cache = Filer.locateTemporary();
        ticker = new Ticker(BitFlyer.FX_BTC_JPY.log(), cache);
    }

    @Test
    public void readTick() throws Exception {
        List<Tick> ticks = ticker.read(start, minite1, TickSpan.Second5, false).toList();
        assert ticks.size() == 12;
    }
}
