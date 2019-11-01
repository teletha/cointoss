/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.verify.VerifiableMarket;

public class MarketTest {

    private VerifiableMarket market = new VerifiableMarket();

    @Test
    void stopWithoutPosition() {
        List<Order> orders = market.stop().toList();

        assert orders.isEmpty();
    }

    @Test
    void stop() {
        market.requestAndExecution(Order.with.buy(1).price(10));

        List<Order> orders = market.stop(s -> s.take()).toList();

        market.perform(Execution.with.sell(1).price(12));
        Order order = orders.get(0);
        assert order.remainingSize.is(0);
        assert order.executedSize.is(1);
    }
}
