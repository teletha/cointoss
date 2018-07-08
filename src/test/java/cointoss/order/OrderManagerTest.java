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

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.backtest.TestableMarketService;

/**
 * @version 2018/07/09 2:07:07
 */
class OrderManagerTest {

    @Test
    void hasActiveOrder() {
        OrderManager orders = create();
        assert orders.hasActiveOrder() == false;
        assert orders.hasNoActiveOrder() == true;

        orders.requestNow(Order.limitLong(1, 10));
        assert orders.hasActiveOrder() == true;
        assert orders.hasNoActiveOrder() == false;
    }

    @Test
    void added() {
        OrderManager orders = create();
        List<Order> added = orders.added.toList();
        assert added.size() == 0;

        orders.requestNow(Order.limitLong(1, 10));
        assert added.size() == 1;
        orders.requestNow(Order.limitLong(1, 10));
        assert added.size() == 2;
    }

    @Test
    void removedByCancel() {
        OrderManager orders = create();
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
    void request() {
        OrderManager orders = create();
        assert orders.items.size() == 0;
        orders.requestNow(Order.limitLong(1, 10));
        assert orders.items.size() == 1;
    }

    @Test
    void cancel() {
        OrderManager orders = create();
        Order order = orders.requestNow(Order.limitLong(1, 10));
        assert orders.items.size() == 1;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
        assert orders.cancelNow(order) == order;
        assert orders.items.size() == 0;
    }

    /**
     * Helper to create {@link OrderManager}.
     * 
     * @return
     */
    private OrderManager create() {
        return new OrderManager(new TestableMarketService());
    }
}
