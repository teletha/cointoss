/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cointoss.Order;
import cointoss.Side;

/**
 * @version 2017/09/02 12:31:41
 */
public class OrderTest {

    @Test
    public void order() throws Exception {
        TestableMarket market = new TestableMarket();
        AtomicInteger counter = new AtomicInteger();

        // order with cancel time
        Order.limitShort(2, 12).entryTo(market).to(entry -> {
            counter.incrementAndGet();
        });

        market.execute(Side.BUY, 1, 10);
        assert counter.get() == 1;
        market.execute(Side.BUY, 1, 12);
        assert counter.get() == 2;
        market.execute(Side.BUY, 1, 12);
        assert counter.get() == 3;
        market.execute(Side.BUY, 1, 12);
        assert counter.get() == 3;
    }
}
