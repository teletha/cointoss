/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;

class VerifiableMarketServiceSchedulerTest {

    @Test
    void schedule() {
        Value v = new Value();

        VerifiableMarket market = new VerifiableMarket();
        ScheduledExecutorService scheduler = market.service.scheduler();
        market.perform(Execution.with.buy(1));

        scheduler.schedule(() -> v.value++, 5, SECONDS);
        assert v.value == 0;

        market.perform(Execution.with.buy(1), 1); // total 1
        assert v.value == 0;

        market.perform(Execution.with.buy(1), 2); // total 3
        assert v.value == 0;

        market.perform(Execution.with.buy(1), 2); // total 5
        assert v.value == 1;
    }

    private static class Value {
        int value = 0;
    }
}
