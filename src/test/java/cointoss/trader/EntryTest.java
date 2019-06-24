/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trader;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;

class EntryTest extends TraderTestSupport {

    @Test
    void holdTime() {
        entryAndExit(Execution.with.buy(1).price(10).date(second(0)), Execution.with.buy(1).price(20).date(second(10)));

        Entry e = latest();
        assert e.holdTime().equals(Duration.ofSeconds(10));
    }

    @Test
    void isTerminated() {
        entry(Execution.with.buy(1).price(10));

        Entry e = latest();
        assert e.isTerminated() == false;
    }

    @Test
    void isEntryTerminated() {
        when(now(), v -> new Entry(Direction.BUY) {
            @Override
            protected void order() {
                order(1, s -> s.make(10));
            }
        });

        Entry e = latest();
        assert e.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert e.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert e.isEntryTerminated() == true;
    }

    @Test
    void isExitTerminated() {
        when(now(), v -> new Entry(Direction.BUY) {
            @Override
            protected void order() {
                order(1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Entry e = latest();
        assert e.isExitTerminated() == false;

        market.perform(Execution.with.buy(1).price(9));
        assert e.isExitTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(21));
        assert e.isExitTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(21));
        assert e.isExitTerminated() == true;
    }
}
