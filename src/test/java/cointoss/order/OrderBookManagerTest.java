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

import cointoss.util.Num;
import cointoss.verify.VerifiableMarketService;

class OrderBookManagerTest {

    @Test
    void findLargetOrderFromLongOnly() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.longs.update(page(10, 5));
        manager.longs.update(page(13, 1));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(18, 4));
        manager.longs.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargetOrderFromLongOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.longs.update(page(10, 2));
        manager.longs.update(page(13, 4));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(18, 4));
        manager.longs.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(18, 4);
    }

    @Test
    void findLargetOrderFromShortOnly() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.shorts.update(page(10, 5));
        manager.shorts.update(page(13, 1));
        manager.shorts.update(page(15, 2));
        manager.shorts.update(page(18, 4));
        manager.shorts.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargetOrderFromShortOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.shorts.update(page(10, 2));
        manager.shorts.update(page(13, 4));
        manager.shorts.update(page(15, 2));
        manager.shorts.update(page(18, 4));
        manager.shorts.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(13, 4);
    }

    @Test
    void findLargetOrder() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.shorts.update(page(20, 3));
        manager.shorts.update(page(19, 1));
        manager.shorts.update(page(18, 2));
        manager.shorts.update(page(17, 8));
        manager.shorts.update(page(16, 3));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(14, 1));
        manager.longs.update(page(13, 5));
        manager.longs.update(page(12, 4));
        manager.longs.update(page(10, 3));

        assert manager.findLargestOrder(10, 20).is(17, 8);
    }

    @Test
    void findLargetOrderWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(new VerifiableMarketService());
        manager.shorts.update(page(10, 2));
        manager.shorts.update(page(13, 4));
        manager.shorts.update(page(15, 2));
        manager.shorts.update(page(18, 4));
        manager.shorts.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(13, 4);
    }

    /**
     * Build order.
     * 
     * @param price
     * @param size
     * @return
     */
    private List<OrderBookPage> page(double price, double size) {
        return List.of(new OrderBookPage(Num.of(price), size));
    }
}
