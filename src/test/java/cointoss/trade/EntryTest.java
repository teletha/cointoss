/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.order.Order;

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
            protected void entry() {
                entry(1, s -> s.make(10));
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
            protected void entry() {
                entry(1, s -> s.make(10));
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

        market.elapse(1, SECONDS);
        market.perform(Execution.with.buy(0.5).price(21));
        assert e.isExitTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(21));
        assert e.isExitTerminated() == true;
    }

    @Test
    void entryWithMultipleExecutionAndExitAtPrice() {
        when(now(), v -> new Entry(Direction.BUY) {
            @Override
            protected void entry() {
                entry(1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Entry e = latest();
        assert e.exits.size() == 0;

        market.perform(Execution.with.buy(0.1).price(9));
        market.perform(Execution.with.buy(0.2).price(9));
        market.perform(Execution.with.buy(0.3).price(9));
        market.perform(Execution.with.buy(0.4).price(9));
        market.elapse(1, SECONDS);
        assert e.exits.size() == 1;
    }

    @Test
    void exitAndStop() {
        when(now(), v -> new Entry(Direction.BUY) {
            @Override
            protected void entry() {
                entry(1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
                exitAt(5);
            }
        });

        Entry e = latest();
        assert e.exits.size() == 0;

        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert e.exits.size() == 1; // exit is ordered
        assert e.entryExecutedSize.is(1);
        assert e.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.1).price(5)); // trigger stop
        market.perform(Execution.with.buy(0.5).price(5));
        assert e.exits.size() == 2; // stop is ordered
        assert e.exits.stream().allMatch(Order::isActive);
        assert e.isExitTerminated() == false;
        assert e.entryExecutedSize.is(1);
        assert e.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.7).price(5));
        assert e.exits.stream().allMatch(Order::isTerminated); // exit is canceled
        assert e.isExitTerminated() == true;
        assert e.entryExecutedSize.is(1);
        assert e.exitExecutedSize.is(1);
    }
}
