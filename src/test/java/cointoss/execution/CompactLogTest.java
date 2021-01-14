/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;
import kiss.I;

class CompactLogTest {

    private static final ZonedDateTime base = Chrono.utc(2021, 1, 1);

    @Test
    void sameAll() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(100).date(base);
        Execution e3 = Execution.with.buy(1).price(100).date(base);

        List<Execution> compact = I.signal(e1, e2, e3).plug(new CompactLog()).toList();
        assert compact.size() == 1;
        assert compact.get(0).size.is(3);
    }

    @Test
    void differentPrice() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(150).date(base);
        Execution e3 = Execution.with.buy(1).price(120).date(base);

        List<Execution> compact = I.signal(e1, e2, e3).plug(new CompactLog()).toList();
        assert compact.size() == 3;
        assert compact.get(0).size.is(1);
        assert compact.get(1).size.is(1);
        assert compact.get(2).size.is(1);
    }

    @Test
    void differentSide() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.sell(1).price(100).date(base);
        Execution e3 = Execution.with.buy(1).price(100).date(base);

        List<Execution> compact = I.signal(e1, e2, e3).plug(new CompactLog()).toList();
        assert compact.size() == 3;
        assert compact.get(0).size.is(1);
        assert compact.get(1).size.is(1);
        assert compact.get(2).size.is(1);
    }

    @Test
    void differentTime() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(100).date(base.plusMinutes(1));
        Execution e3 = Execution.with.buy(1).price(100).date(base.plusMinutes(2));

        List<Execution> compact = I.signal(e1, e2, e3).plug(new CompactLog()).toList();
        assert compact.size() == 3;
        assert compact.get(0).size.is(1);
        assert compact.get(1).size.is(1);
        assert compact.get(2).size.is(1);
    }

    @Test
    void nearTime() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(100).date(base.plus(100, MILLIS));
        Execution e3 = Execution.with.buy(1).price(100).date(base.plus(500, MILLIS));

        List<Execution> compact = I.signal(e1, e2, e3).plug(new CompactLog()).toList();
        assert compact.size() == 1;
        assert compact.get(0).size.is(3);
    }
}
