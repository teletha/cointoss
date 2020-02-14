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

import static java.time.temporal.ChronoUnit.MINUTES;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.order.OrderStrategy.Orderable;

class EntryTest extends TraderTestSupport {

    private Scenario entry(int size, Consumer<Orderable> strategy) {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, strategy);
            }

            @Override
            protected void exit() {
            }
        });

        return scenario();
    }

    @Test
    void makeEntry() {
        // just requesting entry order
        Scenario s = entry(1, o -> o.make(10));
        assert s.entrySize.is(1);
        assert s.entryPrice.is(0);
        assert s.entryExecutedSize.is(0);
        assert s.entryRemainingSize().is(1);
    }

    @Test
    void makeEntryCompleting() {
        // just requesting entry order
        Scenario s = entry(1, o -> o.make(10));

        // complete entry
        executeEntry(1, 10);
        assert s.entrySize.is(1);
        assert s.entryPrice.is(10);
        assert s.entryExecutedSize.is(1);
        assert s.entryRemainingSize().is(0);
    }

    @Test
    void makeEntryCanceling() {
        // just requesting entry order
        Scenario s = entry(1, o -> o.make(10).cancelAfter(10, MINUTES));

        // cancel entry
        market.elapse(10, MINUTES);
        assert s.entrySize.is(1);
        assert s.entryPrice.is(0);
        assert s.entryExecutedSize.is(0);
        assert s.entryRemainingSize().is(0);
    }

}
