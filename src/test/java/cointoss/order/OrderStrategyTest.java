/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import static cointoss.Direction.*;
import static java.util.concurrent.TimeUnit.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import antibug.powerassert.PowerAssertOff;
import cointoss.TestableMarket;
import cointoss.execution.Execution;
import kiss.I;
import kiss.Signaling;
import kiss.WiseConsumer;

class OrderStrategyTest {

    private TestableMarket market;

    @BeforeEach
    void setup() {
        market = new TestableMarket();
    }

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

    @Test
    void cancelWhen() {
        Signaling signaling = new Signaling();
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelWhen(signaling.expose)).toList();

        Order o = orders.get(0);
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(12));
        assert o.state == OrderState.ACTIVE;

        signaling.accept("cancel");
        assert o.state == OrderState.CANCELED;
    }

    @Test
    void next() {
        Signaling signaling = new Signaling();
        List<Order> orders = market.request(BUY, 1, s -> s.make(10).cancelWhen(signaling.expose).next(n -> n.make(20))).toList();

        Order o = orders.get(0);
        assert orders.size() == 1;
        assert o.price.is(10);
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(12));
        assert o.state == OrderState.ACTIVE;

        signaling.accept("cancel");
        assert o.state == OrderState.CANCELED;

        o = orders.get(1);
        assert orders.size() == 2;
        assert o.price.is(20);
        assert o.state == OrderState.ACTIVE;
        market.perform(Execution.with.buy(1).price(11));
        assert o.state == OrderState.COMPLETED;
    }

    @Test
    void nextRecursively() {
        Signaling signaling = new Signaling();

        WiseConsumer<Orderable> strategy = I.recurse((self, s) -> {
            s.make(10).cancelWhen(signaling.expose).next(self);
        });

        List<Order> orders = market.request(BUY, 1, strategy).toList();

        assert orders.size() == 1;
        assert orders.get(0).state == OrderState.ACTIVE;
        signaling.accept("cancel");
        assert orders.get(0).state == OrderState.CANCELED;

        assert orders.size() == 2;
        assert orders.get(1).state == OrderState.ACTIVE;
        signaling.accept("cancel");
        assert orders.get(1).state == OrderState.CANCELED;

        assert orders.size() == 3;
        assert orders.get(2).state == OrderState.ACTIVE;
        signaling.accept("cancel");
        assert orders.get(2).state == OrderState.CANCELED;

        assert orders.size() == 4;
        assert orders.get(3).state == OrderState.ACTIVE;
        signaling.accept("cancel");
        assert orders.get(3).state == OrderState.CANCELED;
    }

    @Test
    void makeLinearCluster() {
        List<Order> orders = market.request(BUY, 4, s -> s.makeCluster(10, 7, Division.Linear4)).toList();
        assert orders.size() == 4;

        Order order0 = orders.get(0);
        assert order0.state == OrderState.ACTIVE;
        assert order0.price.is(10);
        assert order0.size.is(1);

        Order order1 = orders.get(1);
        assert order1.state == OrderState.ACTIVE;
        assert order1.price.is(9);
        assert order1.size.is(1);

        Order order2 = orders.get(2);
        assert order2.state == OrderState.ACTIVE;
        assert order2.price.is(8);
        assert order2.size.is(1);

        Order order3 = orders.get(3);
        assert order3.state == OrderState.ACTIVE;
        assert order3.price.is(7);
        assert order3.size.is(1);

        market.perform(Execution.with.buy(1).price(5));
        assert order0.state == OrderState.COMPLETED;
        assert order1.state == OrderState.ACTIVE;
        assert order2.state == OrderState.ACTIVE;
        assert order3.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(5));
        market.perform(Execution.with.buy(1).price(5));
        market.perform(Execution.with.buy(1).price(5));
        assert order0.state == OrderState.COMPLETED;
        assert order1.state == OrderState.COMPLETED;
        assert order2.state == OrderState.COMPLETED;
        assert order3.state == OrderState.COMPLETED;
    }

    @Test
    @PowerAssertOff
    void cancelLinearCluster() {
        List<Order> orders = market.request(BUY, 4, s -> s.makeCluster(10, 7, Division.Linear4).cancelAfter(2, SECONDS)).toList();
        assert orders.size() == 4;

        Order order0 = orders.get(0);
        assert order0.state == OrderState.ACTIVE;
        assert order0.price.is(10);
        assert order0.size.is(1);

        Order order1 = orders.get(1);
        assert order1.state == OrderState.ACTIVE;
        assert order1.price.is(9);
        assert order1.size.is(1);

        Order order2 = orders.get(2);
        assert order2.state == OrderState.ACTIVE;
        assert order2.price.is(8);
        assert order2.size.is(1);

        Order order3 = orders.get(3);
        assert order3.state == OrderState.ACTIVE;
        assert order3.price.is(7);
        assert order3.size.is(1);

        market.perform(Execution.with.buy(1).price(5), 1); // total 1
        assert order0.state == OrderState.COMPLETED;
        assert order1.state == OrderState.ACTIVE;
        assert order2.state == OrderState.ACTIVE;
        assert order3.state == OrderState.ACTIVE;

        market.perform(Execution.with.buy(1).price(5), 1); // total 2
        assert order0.state == OrderState.COMPLETED;
        assert order1.state == OrderState.COMPLETED;
        assert order2.state == OrderState.CANCELED;
        assert order3.state == OrderState.CANCELED;
    }
}