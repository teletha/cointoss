/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static cointoss.MarketTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.backtest.TestableMarket;
import cointoss.util.Chrono;
import kiss.I;

/**
 * @version 2018/05/26 10:37:10
 */
class MarketLogTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    TestableMarket market = new TestableMarket();

    MarketService service = market.service;

    MarketLog log = new MarketLog(service, room.root);

    @Test
    void logAtNoServicedDate() {
        assert log.at(2016, 1, 1).toList().isEmpty();
    }

    @Test
    void logAtServicedDateWithoutExecutions() {
        assert log.at(Chrono.utcNow()).toList().isEmpty();
    }

    @Test
    void readNoLog() {
        ZonedDateTime today = Chrono.utcNow();

        List<Execution> list = log.at(today).toList();
        assert list.isEmpty() == true;
    }

    @Test
    void readLog() {
        ZonedDateTime today = Chrono.utcNow();
        List<Execution> original = writeExecutionLog(today);
        List<Execution> restored = log.at(today).toList();

        assertIterableEquals(original, restored);
    }

    @Test
    void readCompactLog() {
        ZonedDateTime today = Chrono.utcNow();
        List<Execution> original = writeCompactExecutionLog(today);
        List<Execution> restored = log.at(today).toList();

        assertIterableEquals(original, restored);
    }

    @Test
    void createCompactLogFromNormalLogAutomatically() {
        ZonedDateTime today = Chrono.utcNow();
        ZonedDateTime yesterday = today.minusDays(1);

        List<Execution> yesterdayLog = writeExecutionLog(yesterday);
        Path yesterdayCompactLog = log.locateCompactLog(yesterday);
        assert Files.notExists(yesterdayCompactLog);

        // reading yesterday log will NOT create compact log automatically
        // because next day's log doesn't exist
        List<Execution> restored = log.at(yesterday).toList();
        assertIterableEquals(restored, yesterdayLog);
        assert Files.notExists(yesterdayCompactLog);

        // reading yesterday log will create compact log automatically
        // because next day's log exist
        writeExecutionLog(today);

        restored = log.at(yesterday).toList();
        assertIterableEquals(restored, yesterdayLog);
        assert Files.exists(yesterdayCompactLog);
    }

    /**
     * Create dummy execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeExecutionLog(ZonedDateTime date) {
        List<Execution> list = executionRandomly(10).toList();

        try {
            Files.write(log.locateLog(date), I.signal(list).map(Execution::toString).toList());
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return list;
    }

    /**
     * Create dummy compact execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeCompactExecutionLog(ZonedDateTime date) {
        List<Execution> list = executionRandomly(10).toList();
        log.writeCompactLog(date, I.signal(list));
        return list;
    }
}
