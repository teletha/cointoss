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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    void compactSize() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1));
        exes.add(buy(10, 1));
        exes.add(buy(10, 2));
        exes.add(buy(10, 5.4));
        exes.add(buy(10, 3));
        exes.add(buy(10, 0.1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactPrice() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1));
        exes.add(buy(10, 1));
        exes.add(buy(12, 1));
        exes.add(buy(14.5, 1));
        exes.add(buy(10, 1));
        exes.add(buy(3.33, 1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSameSide() {
        Execution first = buy(10, 1);
        Execution second = buy(10, 1);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactDiffSide() {
        Execution first = buy(10, 1);
        Execution second = sell(10, 1);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactSameConsecutiveType() {
        Execution first = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);
        Execution second = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactDiffConsecutiveType() {
        Execution first = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);
        Execution second = buy(10, 1).consecutive(Execution.ConsecutiveDifference);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
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
