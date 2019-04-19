/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.execution.Executed;
import cointoss.verify.VerifiableMarketService;

class OrderManagerTest {

    private VerifiableMarketService service;

    private OrderManager orders;

    @BeforeEach
    void init() {
        service = new VerifiableMarketService();
        orders = new OrderManager(service);
    }

    @Test
    void request() {
        assert orders.items.size() == 0;
        orders.requestNow(Order.buy(1).price(10));
        assert orders.items.size() == 1;
    }

    @Test
    void cancel() {
        Order order = orders.requestNow(Order.buy(1).price(10));
        assert orders.items.size() == 1;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
    }

    @Test
    void hasActiveOrder() {
        assert orders.hasActiveOrder() == false;
        assert orders.hasNoActiveOrder() == true;

        orders.requestNow(Order.buy(1).price(10));
        assert orders.hasActiveOrder() == true;
        assert orders.hasNoActiveOrder() == false;
    }

    @Test
    void added() {
        List<Order> added = orders.added.toList();
        assert added.size() == 0;

        orders.requestNow(Order.buy(1).price(10));
        assert added.size() == 1;
        orders.requestNow(Order.buy(1).price(10));
        assert added.size() == 2;
    }

    @Test
    void removedByCancel() {
        Order order1 = orders.requestNow(Order.buy(1).price(10));
        Order order2 = orders.requestNow(Order.buy(1).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        orders.cancelNow(order1);
        assert removed.size() == 1;
        orders.cancelNow(order2);
        assert removed.size() == 2;
    }

    @Test
    void removedByExecute() {
        orders.requestNow(Order.buy(1).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        service.emulate(Executed.sell(1).price(10));
        assert removed.size() == 1;
    }

    @Test
    void removedByExecuteDividedly() {
        orders.requestNow(Order.buy(2).price(10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        service.emulate(Executed.sell(1).price(10));
        assert removed.size() == 0;
        service.emulate(Executed.sell(1).price(10));
        assert removed.size() == 1;
    }
}
