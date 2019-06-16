/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import static cointoss.Direction.*;
import static java.time.temporal.ChronoUnit.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.verify.VerifiableMarket;

class OrderStrategyTest {

    private VerifiableMarket market = new VerifiableMarket();

    private boolean completed = false;

    @Test
    void make() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10)).effectOnComplete(() -> completed = true).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;
        assert completed == true;

        market.perform(Execution.with.buy(1).price(9));
        assert o.state == OrderState.COMPLETED;
        assert o.price.is(10);
    }

    @Test
    void take() {
        List<Order> orders = market.request(BUY, 1, s -> s.take()).effectOnComplete(() -> completed = true).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;
        assert completed == true;

        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.COMPLETED;
        assert o.price.is(11);
    }

    @Test
    void cancelAfter() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelAfter(3, SECONDS))
                .effectOnComplete(() -> completed = true)
                .toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;
        assert completed == false;

        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.COMPLETED;
        assert o.price.is(11);
    }
}
