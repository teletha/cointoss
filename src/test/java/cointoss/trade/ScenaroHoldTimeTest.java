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

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.execution.Execution;

class ScenaroHoldTimeTest extends TraderTestSupport {

    @Test
    void holdTime() {
        Scenario s = entry(1, o -> o.make(10));
        executeEntryAll();
        market.elapse(10, ChronoUnit.SECONDS);
        exit(o -> o.make(20));
        executeExitAll();

        assert s.holdTime().equals(Duration.ofSeconds(10));
    }

    @Test
    void holdTimeWithNoneExecutedActiveEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        market.elapse(14, SECONDS);

        Scenario s = latest();
        assert s.holdTime().equals(Duration.ofSeconds(14));
    }

    @Test
    void holdTimeWithNoneExecutedCanceledEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10).cancelAfter(8, SECONDS));
            }

            @Override
            protected void exit() {
            }
        });

        market.elapse(18, SECONDS);

        Scenario s = latest();
        assert s.holdTime().isZero();
    }

    @Test
    void holdTimeWithPartialyExecutedCanceledEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10).cancelAfter(8, SECONDS));
            }

            @Override
            protected void exit() {
            }
        });

        market.perform(Execution.with.buy(0.1).price(9));
        market.elapse(18, SECONDS);

        Scenario s = latest();
        assert s.holdTime().equals(Duration.ofSeconds(18));
    }
}
