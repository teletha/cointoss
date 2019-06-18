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

import static cointoss.Direction.BUY;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.verify.VerifiableMarket;

class OrderStrategyTest {

    private VerifiableMarket market = new VerifiableMarket();

    @Test
    void make() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10)).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(9));
        assert o.state == OrderState.COMPLETED;
        assert o.price.is(10);
    }

    @Test
    void take() {
        List<Order> orders = market.request(BUY, 1, s -> s.take()).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.COMPLETED;
        assert o.price.is(11);
    }

    @Test
    void cancelAfter() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelAfter(3, SECONDS)).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(11), 1);// total 1
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(12), 1); // total 2
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(13), 1); // total 3
        assert o.state == OrderState.CANCELED;
    }

    @Test
    void makeBeforeCancel() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelAfter(3, SECONDS)).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(11), 1);// total 1
        assert o.state == OrderState.ACTIVE;
        assert o.executedSize.is(0);
        market.perform(Execution.with.buy(1).price(9), 1); // total 2
        assert o.state == OrderState.COMPLETED;
        assert o.executedSize.is(1);
        market.perform(Execution.with.buy(1).price(13), 1); // total 3
        assert o.state == OrderState.COMPLETED;
    }

    @Test
    void makePartialyBeforeCancel() {
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelAfter(3, SECONDS)).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(0.5).price(11), 1);// total 1
        assert o.state == OrderState.ACTIVE;
        assert o.executedSize.is(0);
        market.perform(Execution.with.buy(0.5).price(9), 1); // total 2
        assert o.state == OrderState.ACTIVE;
        assert o.executedSize.is(0.5);
        market.perform(Execution.with.buy(0.5).price(13), 1); // total 3
        assert o.state == OrderState.CANCELED;
        assert o.executedSize.is(0.5);
    }
}
