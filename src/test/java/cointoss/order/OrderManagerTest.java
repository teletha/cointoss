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

    /**
     * Helper to create {@link OrderManager}.
     * 
     * @return
     */
    private OrderManager create() {
        return new OrderManager(new TestableMarketService());
    }
}
