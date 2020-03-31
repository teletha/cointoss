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

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;

public class StopTest extends TraderTestSupport {

    @Test
    void stop() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 2, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(5);
            }
        });

        Scenario s = latest();
        assert s.exits.size() == 0;

        // execute entry
        market.perform(Execution.with.buy(2).price(9));
        awaitOrderBufferingTime();

        // trigger stop loss
        market.perform(Execution.with.buy(3).price(4));
        assert s.exits.size() == 1; // stop is ordered
        assert s.exitExecutedSize.is(0);
        assert s.exitSize.is(2);
        assert s.exitPrice.is(0);

        // execute stop loss
        market.perform(Execution.with.buy(3).price(4));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(2);
        assert s.exitSize.is(2);
        assert s.exitPrice.is(4);
    }

    @Test
    void stopWillUseTheWorstExecutionPrice() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 2, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(5);
            }
        });

        Scenario s = latest();
        assert s.exits.size() == 0;

        // execute entry
        market.perform(Execution.with.buy(2).price(9));
        awaitOrderBufferingTime();

        // trigger stop loss
        market.perform(Execution.with.buy(3).price(4));
        assert s.exits.size() == 1; // stop is ordered
        assert s.exitExecutedSize.is(0);
        assert s.exitSize.is(2);
        assert s.exitPrice.is(0);

        // execute stop loss
        market.perform(Execution.with.buy(3).price(10));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(2);
        assert s.exitSize.is(2);
        assert s.exitPrice.is(4);
    }
}
