/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import static cointoss.MarketTestSupport.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.backtest.VerifiableMarketService;

/**
 * @version 2018/07/09 2:07:07
 */
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
        orders.requestNow(Order.limitLong(1, 10));
        assert orders.items.size() == 1;
    }

    @Test
    void cancel() {
        Order order = orders.requestNow(Order.limitLong(1, 10));
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

        orders.requestNow(Order.limitLong(1, 10));
        assert orders.hasActiveOrder() == true;
        assert orders.hasNoActiveOrder() == false;
    }

    @Test
    void added() {
        List<Order> added = orders.added.toList();
        assert added.size() == 0;

        orders.requestNow(Order.limitLong(1, 10));
        assert added.size() == 1;
        orders.requestNow(Order.limitLong(1, 10));
        assert added.size() == 2;
    }

    @Test
    void removedByCancel() {
        Order order1 = orders.requestNow(Order.limitLong(1, 10));
        Order order2 = orders.requestNow(Order.limitLong(1, 10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        orders.cancelNow(order1);
        assert removed.size() == 1;
        orders.cancelNow(order2);
        assert removed.size() == 2;
    }

    @Test
    void removedByExecute() {
        orders.requestNow(Order.limitLong(1, 10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        service.emulate(sell(1, 10));
        assert removed.size() == 1;
    }

    @Test
    void removedByExecuteDividedly() {
        orders.requestNow(Order.limitLong(2, 10));

        List<Order> removed = orders.removed.toList();
        assert removed.size() == 0;
        service.emulate(sell(1, 10));
        assert removed.size() == 0;
        service.emulate(sell(1, 10));
        assert removed.size() == 1;
    }
}
