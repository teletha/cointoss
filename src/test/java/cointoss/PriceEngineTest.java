/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.verify.VerifiableMarket;
import hypatia.Num;

public class PriceEngineTest {

    private VerifiableMarket market = new VerifiableMarket();

    @Test
    void action() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.ONE, Direction.BUY, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(10));
        assert counter.get() == 1;
    }

}
