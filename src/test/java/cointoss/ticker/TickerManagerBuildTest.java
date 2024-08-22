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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.market.TestableMarketService;
import cointoss.util.Chrono;
import cointoss.util.feather.FeatherStore;

public class TickerManagerBuildTest {

    @Test
    void build() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2, 23, 59, 0, 0);

        TestableMarketService service = new TestableMarketService();
        service.log.createFastLog(start, end, Span.Minute1, false);

        TickerManager manager = new TickerManager(service);
        manager.build(start, end, false);
        assert manager.on(Span.Day).ticks.query(start, end).toList().size() == 2;
        assert manager.on(Span.Hour4).ticks.query(start, end).toList().size() == 12;
        assert manager.on(Span.Hour1).ticks.query(start, end).toList().size() == 48;
        assert manager.on(Span.Minute15).ticks.query(start, end).toList().size() == 192;
        assert manager.on(Span.Minute5).ticks.query(start, end).toList().size() == 576;
    }

    @Test
    void rebuild() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2, 23, 59, 0, 0);

        TestableMarketService service = new TestableMarketService();
        service.log.createFastLog(start, end, Span.Minute1, false);

        TickerManager manager = new TickerManager(service);
        FeatherStore<Tick> ticks = manager.on(Span.Minute15).ticks;

        manager.build(start, end, false);
        List<Tick> oldTicks = ticks.query(start, end).toList();

        // service.log.createFastLog(start, end, Span.Minute1, false);
        manager.build(start, end, false);
        ticks.clearHeap();
        List<Tick> sameTicks = ticks.query(start, end).toList();
        Assertions.assertIterableEquals(oldTicks, sameTicks);

        // service.log.createFastLog(start, end, Span.Minute1, false);
        // manager.build(start, end, true);
        // List<Tick> newTicks = ticks.query(start, end).toList();
        // Assertions.assertIterableEquals(oldTicks, newTicks);
    }
}
