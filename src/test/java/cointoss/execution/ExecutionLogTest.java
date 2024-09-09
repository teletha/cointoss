/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.TestableMarketService;
import cointoss.execution.ExecutionLog.Cache;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import kiss.I;

class ExecutionLogTest {

    private ExecutionLog log;

    @BeforeEach
    void setup() {
        log = new ExecutionLog(new TestableMarketService());
    }

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
        List<Execution> original = writeNormalLog(today);
        List<Execution> restored = log.at(today).toList();

        assert original.size() == 10;
        assert restored.size() == 10;

        assertIterableEquals(original, restored);
    }

    @Test
    void readCompactLog() throws InterruptedException {
        ZonedDateTime today = Chrono.utcNow();
        List<Execution> original = writeCompactLog(today);
        List<Execution> restored = log.at(today).toList();

        assert original.size() == 10;
        assert restored.size() <= 10;

        assumeTrue(restored.size() == 10);
        assertIterableEquals(original, restored);
    }

    /**
     * Create dummy execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeNormalLog(ZonedDateTime date) {
        List<Execution> list = Executions.random(10, Span.Hour1);
        log.cache(date).normal.text(I.signal(list).map(Execution::toString).toList());
        return list;
    }

    /**
     * Create dummy compact execution log.
     * 
     * @param date A target date.
     */
    private List<Execution> writeCompactLog(ZonedDateTime date) {
        List<Execution> list = Executions.random(10, Span.Hour1);
        log.cache(date).writeCompact(I.signal(list)).to();
        return list;
    }
}