/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.market.TestableMarketService;
import cointoss.util.Chrono;

public class TickerManagerBuildTest {

    @Test
    void build() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 3);

        TestableMarketService service = new TestableMarketService();
        service.log.createFastLog(start, end, Span.Minute1);

        TickerManager manager = new TickerManager(service);
        manager.build(start, end, false);

        Ticker ticker = manager.on(Span.Day);
        assert ticker.ticks.query(start, end).toList().size() == 2;
    }

    @Test
    void rebuild() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 3);

        TestableMarketService service = new TestableMarketService();
        service.log.createFastLog(start, end, Span.Minute1);

        TickerManager manager = new TickerManager(service);
        manager.build(start, end, false);

        Ticker ticker = manager.on(Span.Hour4);
        List<Tick> build = ticker.ticks.query(start, end).toList();
        assert build.size() == 24;
    }
}
