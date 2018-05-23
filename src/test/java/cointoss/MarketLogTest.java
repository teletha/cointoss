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
import kiss.Signal;

/**
 * @version 2018/05/08 13:09:15
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
    void readNone() {
        Signal<Execution> exe = log.read(ZonedDateTime.now());
        assert exe.toList().isEmpty();
    }

    @Test
    void readCompressedCache() {
        ZonedDateTime date = ZonedDateTime.now();

        createCompressedCache(date);

        Signal<Execution> exe = log.read(date);
        assert exe.toList().isEmpty();
    }

    /**
     * Create dummy compressed cache.
     * 
     * @param date
     */
    private void createCompressedCache(ZonedDateTime date) {
        try {
            Path compressed = log.locateCompressedLog(date);

            // create file
            Files.createFile(compressed);

            // write dummy log
            List<Execution> executions = MarketTestSupport.executionSerially(10, Side.BUY, 10, 1);

            for (Execution execution : executions) {
                log.writeLog(execution);
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
