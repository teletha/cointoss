/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.position;

import org.junit.jupiter.api.Test;

import cointoss.order.Order;
import cointoss.verify.VerifiableMarket;

class EntryManagerTest {

    VerifiableMarket market = new VerifiableMarket();

    EntryManager manager = market.entries;

    @Test
    void size() {
        market.request(Order.buy(1).price(10)).to(order -> {
            assert manager.size.v.is(0);
        });
    }
}
