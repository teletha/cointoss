/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import org.junit.jupiter.api.Test;

import cointoss.order.Order;
import cointoss.order.OrderState;

/**
 * @version 2018/04/02 16:49:22
 */
class OrderSetTest {

    @Test
    void set() {
        Order o1 = Order.with.buy(1).price(100).state(OrderState.ACTIVE);
        Order o2 = Order.with.buy(1).price(200).state(OrderState.ACTIVE);

        OrderSet set = new OrderSet();
        set.sub.add(o1);
        set.sub.add(o2);

        assert set.amount.get().is(2);
        assert set.averagePrice.get().is(150);

        // emulate cancel
        set.sub.remove(0);

        assert set.amount.get().is(1);
        assert set.averagePrice.get().is(200);
    }
}
