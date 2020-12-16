/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.execution.ExecutionLog.Cache;
import cointoss.util.Chrono;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;
import psychopath.Locator;

class CacheTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    VerifiableMarket market = new VerifiableMarket();

    ExecutionLog log = new ExecutionLog(market.service, Locator.directory(room.root));

    @Test
    void normalLog() {
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        assert cache.normal.name().equals("execution20201215.log");
    }

    @Test
    void compactLog() {
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        assert cache.compact.name().equals("execution20201215.clog");
    }

    @Test
    void fastLog() {
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        assert cache.fast.name().equals("execution20201215.flog");
    }

    @Test
    void writeNormal() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        assert cache.existNormal() == false;

        cache.writeNormal(e1, e2);
        assert cache.existNormal();
    }

    @Test
    void writeCompact() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        assert cache.existCompact() == false;

        cache.writeCompact(e1, e2);
        assert cache.existCompact();
    }

    @Test
    void readNormal() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeNormal(e1, e2);

        // read
        List<Execution> executions = cache.readNormal().toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(e1);
        assert executions.get(1).equals(e2);
    }

    @Test
    void readCompact() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeCompact(e1, e2);

        // read
        List<Execution> executions = cache.readCompact().toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(e1);
        assert executions.get(1).equals(e2);
    }

    @Test
    void readFromNotNormalButCompact() {
        Execution n1 = Execution.with.buy(1).price(10);
        Execution n2 = Execution.with.buy(1).price(12);
        Execution c1 = Execution.with.buy(2).price(10);
        Execution c2 = Execution.with.buy(2).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeNormal(n1, n2);
        cache.writeCompact(c1, c2);

        // read
        List<Execution> executions = cache.read().toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(c1);
        assert executions.get(1).equals(c2);
    }

    @Test
    void readExternalRepository() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        ExecutionLogRepository external = new ExecutionLogRepository(market.service) {

            @Override
            public Signal<Execution> convert(ZonedDateTime date) {
                return I.signal(e1, e2);
            }

            @Override
            public Signal<ZonedDateTime> collect() {
                return I.signal(Chrono.utc(2020, 12, 15));
            }
        };

        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        List<Execution> executions = cache.readExternalRepository(external).toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(e1);
        assert executions.get(1).equals(e2);
        assert cache.existNormal();
        assert cache.existCompact() == false;
    }

    @Test
    void convertNormalToCompact() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeNormal(e1, e2);
        assert cache.existNormal();
        assert cache.existCompact() == false;

        // convert
        cache.convertNormalToCompact();
        assert cache.existNormal() == false;
        assert cache.existCompact();

        // read from compact
        List<Execution> executions = cache.read().toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(e1);
        assert executions.get(1).equals(e2);
    }

    @Test
    void convertCompactToNormal() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeCompact(e1, e2);
        assert cache.existNormal() == false;
        assert cache.existCompact();

        // convert
        cache.convertCompactToNormal();
        assert cache.existNormal();
        assert cache.existCompact();

        // read from normal
        List<Execution> executions = cache.readNormal().toList();
        assert executions.size() == 2;
        assert executions.get(0).equals(e1);
        assert executions.get(1).equals(e2);
    }

    @Test
    void convertCompactToFast() {
        Execution e1 = Execution.with.buy(1).price(10);
        Execution e2 = Execution.with.buy(1).price(12);

        // write
        Cache cache = log.cache(Chrono.utc(2020, 12, 15));
        cache.writeCompact(e1, e2);
        assert cache.existCompact();
        assert cache.existFast() == false;

        // convert
        cache.convertCompactToFast();
        assert cache.existCompact();
        assert cache.existFast();

        // read from fast
        List<Execution> executions = cache.readFast().toList();
        assert executions.size() == 4;
    }
}
