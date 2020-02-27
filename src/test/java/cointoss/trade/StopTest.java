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

import antibug.powerassert.PowerAssertOff;
import cointoss.Direction;
import cointoss.execution.Execution;

public class StopTest extends TraderTestSupport {

    @Test
    @PowerAssertOff
    void testName() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 2, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(15);
                exitAt(5);
            }
        });

        Scenario s = latest();
        assert s.exits.size() == 0;

        market.perform(Execution.with.buy(2).price(9));
        awaitOrderBufferingTime();
        assert s.exits.size() == 1; // exit is ordered
        assert s.entryExecutedSize.is(2);
        assert s.exitExecutedSize.is(0);
        assert s.exitSize.is(2);

        market.perform(Execution.with.buy(1).price(16)); // execute stop profit
        assert s.exitExecutedSize.is(1);
        assert s.exitSize.is(2);

        market.perform(Execution.with.buy(3).price(4)); // trigger stop loss
        awaitOrderBufferingTime();
        assert s.exits.size() == 2; // stop is ordered
        assert s.entryExecutedSize.is(2);
        assert s.exitExecutedSize.is(1);
        assert s.exitSize.is(2) : s;
    }
}
