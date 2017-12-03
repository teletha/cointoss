/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import org.junit.Test;

import cointoss.Order;

/**
 * @version 2017/12/02 1:37:21
 */
public class OrderSetTest {

    @Test
    public void set() throws Exception {
        Order o1 = Order.limitLong(1, 100);
        Order o2 = Order.limitLong(1, 200);

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
