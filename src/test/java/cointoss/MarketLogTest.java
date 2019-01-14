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

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.util.Chrono;
import kiss.I;
import psychopath.Locator;

/**
 * @version 2018/05/26 10:37:10
 */
class MarketLogTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    VerifiableMarket market = new VerifiableMarket();

    MarketService service = market.service;

    MarketLog log = new MarketLog(service, Locator.directory(room.root));

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

    /**
     * Create dummy execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeExecutionLog(ZonedDateTime date) {
        List<Execution> list = executionRandomly(10).toList();
        log.locateLog(date).text(I.signal(list).map(Execution::toString).toList());
        return list;
    }

    /**
     * Create dummy compact execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeCompactExecutionLog(ZonedDateTime date) {
        return log.cache(date).compact(executionRandomly(10)).toList();
    }
}
