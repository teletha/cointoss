/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Currency;
import cointoss.MarketSetting;
import cointoss.verify.VerifiableMarketService;

class OrderBookManagerTest {

    private VerifiableMarketService service = new VerifiableMarketService(MarketSetting.with.derivative()
            .target(Currency.UNKNOWN.minimumSize(1).scale(1))
            .base(Currency.UNKNOWN.minimumSize(1).scale(1)));

    @Test
    void findLargestOrderFromLongOnly() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.longs.update(page(10, 5));
        manager.longs.update(page(13, 1));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(18, 4));
        manager.longs.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargestOrderFromLongOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.longs.update(page(10, 2));
        manager.longs.update(page(13, 4));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(18, 4));
        manager.longs.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(18, 4);
    }

    @Test
    void findLargestOrderFromShortOnly() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(page(10, 5));
        manager.shorts.update(page(13, 1));
        manager.shorts.update(page(15, 2));
        manager.shorts.update(page(18, 4));
        manager.shorts.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargestOrderFromShortOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(page(10, 2));
        manager.shorts.update(page(13, 4));
        manager.shorts.update(page(15, 2));
        manager.shorts.update(page(18, 4));
        manager.shorts.update(page(20, 3));

        assert manager.findLargestOrder(10, 20).is(13, 4);
    }

    @Test
    void findLargestOrder() {
        OrderBookManager manager = new OrderBookManager(service);
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
    void findLargestOrderWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(page(20, 3));
        manager.shorts.update(page(19, 1));
        manager.shorts.update(page(18, 2));
        manager.shorts.update(page(17, 8));
        manager.shorts.update(page(16, 3));
        manager.longs.update(page(15, 2));
        manager.longs.update(page(14, 1));
        manager.longs.update(page(13, 5));
        manager.longs.update(page(12, 8));
        manager.longs.update(page(10, 3));

        assert manager.findLargestOrder(10, 20).is(12, 8);
    }

    /**
     * Build order.
     * 
     * @param price
     * @param size
     * @return
     */
    private List<OrderBookPage> page(float price, float size) {
        return List.of(new OrderBookPage(price, size));
    }
}