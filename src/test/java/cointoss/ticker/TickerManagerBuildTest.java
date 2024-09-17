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

import static antibug.Tester.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.TestableMarketService;
import cointoss.util.Chrono;
import cointoss.util.feather.FeatherStore;

public class TickerManagerBuildTest {

    @Test
    void buildFully() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2, 23, 59, 0, 0);

        TestableMarketService service = new TestableMarketService();
        service.log.generateFastLog(start, end, Span.Minute1);

        TickerManager manager = new TickerManager(service);
        manager.buildFully(false).to();
        assert manager.on(Span.Day).ticks.query(start, end).toList().size() == 2;
        assert manager.on(Span.Hour4).ticks.query(start, end).toList().size() == 12;
        assert manager.on(Span.Hour1).ticks.query(start, end).toList().size() == 48;
        assert manager.on(Span.Minute15).ticks.query(start, end).toList().size() == 192;
        assert manager.on(Span.Minute5).ticks.query(start, end).toList().size() == 0;
        assert manager.on(Span.Minute1).ticks.query(start, end).toList().size() == 0;
    }

    @Test
    void buildByRange() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2, 23, 59, 0, 0);

        TestableMarketService service = new TestableMarketService();
        service.log.generateFastLog(start, end, Span.Minute1);

        TickerManager manager = new TickerManager(service);
        manager.build(start, end, false).to();
        assert manager.on(Span.Day).ticks.query(start, end).toList().size() == 2;
        assert manager.on(Span.Hour4).ticks.query(start, end).toList().size() == 12;
        assert manager.on(Span.Hour1).ticks.query(start, end).toList().size() == 48;
        assert manager.on(Span.Minute15).ticks.query(start, end).toList().size() == 192;
        assert manager.on(Span.Minute5).ticks.query(start, end).toList().size() == 0;
        assert manager.on(Span.Minute1).ticks.query(start, end).toList().size() == 0;
    }

    @Test
    void rebuildByRange() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 2, 23, 59, 0, 0);

        // generate fast log
        TestableMarketService service = new TestableMarketService();
        service.log.generateFastLog(start, end, Span.Hour1);

        TickerManager manager = new TickerManager(service);
        FeatherStore<Tick> ticks = manager.on(Span.Minute15).ticks;

        // build ticker data from fast log
        manager.build(start, end, false).to();
        List<Tick> oldTicks = ticks.query(start, end).toList();
        List<Tick> newTicks = ticks.query(start, end).toList();
        assert same(oldTicks, newTicks);

        // clear memory cache and re-query data from same ticker data
        ticks.clear();
        newTicks = ticks.query(start, end).toList();
        assert same(oldTicks, newTicks);

        // clear memory cache and rebuild ticker data from same fast log
        ticks.clear();
        manager.build(start, end, false).to();
        newTicks = ticks.query(start, end).toList();
        assert same(oldTicks, newTicks);

        // clear memory cache and rebuild ticker data from same fast log
        ticks.clear();
        manager.build(start, end, true).to();
        newTicks = ticks.query(start, end).toList();
        assert same(oldTicks, newTicks);

        // clear memory cache and rebuild ticker data from new generated fast log
        ticks.clear();
        service.log.generateFastLog(start, end, Span.Hour1);
        manager.build(start, end, true).to();
        newTicks = ticks.query(start, end).toList();
        assert different(oldTicks, newTicks);
    }

    @Test
    void outOfRange() {
        ZonedDateTime start = Chrono.utc(2020, 1, 1);
        ZonedDateTime end = Chrono.utc(2020, 1, 7);
        ZonedDateTime insideStart = start.plusDays(2);
        ZonedDateTime insideEnd = end.minusDays(2);

        // generate fast log
        TestableMarketService service = new TestableMarketService();
        service.log.generateFastLog(start, end, Span.Hour1);

        TickerManager manager = new TickerManager(service);
        FeatherStore<Tick> ticks = manager.on(Span.Day).ticks;

        // build inaide only
        manager.build(insideStart, insideEnd, false).to();
        assert ticks.first().date().equals(insideStart);
        assert ticks.last().date().equals(insideEnd);
        List<Tick> inside = ticks.query(insideStart, insideEnd).toList();

        // build with previous data
        manager.build(start, insideEnd, false).to();
        assert ticks.first().date().equals(start);
        assert ticks.last().date().equals(insideEnd);
        assert same(inside, ticks.query(insideStart, insideEnd).toList());

        // build with next data
        manager.build(insideStart, end, false).to();
        assert ticks.first().date().equals(start);
        assert ticks.last().date().equals(end);
        assert same(inside, ticks.query(insideStart, insideEnd).toList());
    }
}
