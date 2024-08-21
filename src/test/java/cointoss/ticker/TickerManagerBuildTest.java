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

import org.junit.jupiter.api.Test;

import cointoss.market.TestableMarketService;
import cointoss.util.Chrono;

public class TickerManagerBuildTest {

    @Test
    void build() {
        TestableMarketService service = new TestableMarketService();

        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2);

        TickerManager manager = new TickerManager(service);
        Ticker ticker = manager.on(Span.Day);
        assert ticker.ticks.at(start) == null;

        service.log.createFastLog(start, end, Span.Minute1);
        manager.build(start, end, false);
        assert ticker.ticks.at(start) != null;
    }
}
