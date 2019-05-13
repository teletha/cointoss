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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.util.Num;

class MyOrderTest {

    @Test
    void direction() {
        assert MyOrder.buy(1).direction.isBuy();
        assert MyOrder.sell(1).direction.isSell();
        assert MyOrder.of(Direction.BUY, 1).direction.isBuy();
        assert MyOrder.of(Direction.SELL, 1).direction.isSell();

        assertThrows(IllegalArgumentException.class, () -> MyOrder.of(null, 1));
    }

    @Test
    void size() {
        assert MyOrder.buy(1).size.is(1);
        assert MyOrder.buy(2.3).size.is(2.3);

        assertThrows(IllegalArgumentException.class, () -> MyOrder.buy(0));
        assertThrows(IllegalArgumentException.class, () -> MyOrder.buy(-1));
        assertThrows(IllegalArgumentException.class, () -> MyOrder.buy(null));
    }

    @Test
    void price() {
        assert MyOrder.buy(1).price(1L).price.is(1);
        assert MyOrder.buy(1).price(2.3).price.is(2.3);
        assert MyOrder.buy(1).price(Num.ONE).price.is(1);

        assert MyOrder.buy(1).price(0).price.is(0);
        assert MyOrder.buy(1).price(-1).price.is(0);
        assert MyOrder.buy(1).price(null).price.is(0);
    }

    @Test
    void condition() {
        assert MyOrder.buy(1).condition == QuantityCondition.GoodTillCanceled;
        assert MyOrder.buy(1).type(QuantityCondition.FillOrKill).condition == QuantityCondition.FillOrKill;
        assert MyOrder.buy(1).type(null).condition == QuantityCondition.GoodTillCanceled;
    }
}
