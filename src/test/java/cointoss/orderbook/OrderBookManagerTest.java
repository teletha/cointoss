/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.orderbook;

import static cointoss.orderbook.OrderBookChanges.*;

import org.junit.jupiter.api.Test;

import cointoss.Currency;
import cointoss.MarketSetting;
import cointoss.TestableMarketService;

class OrderBookManagerTest {

    private TestableMarketService service = new TestableMarketService(MarketSetting.with.derivative()
            .target(Currency.UNKNOWN.minimumSize(1).scale(1))
            .base(Currency.UNKNOWN.minimumSize(1).scale(1)));

    @Test
    void findLargestOrderFromLongOnly() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.longs.update(singleBid(10, 5));
        manager.longs.update(singleBid(13, 1));
        manager.longs.update(singleBid(15, 2));
        manager.longs.update(singleBid(18, 4));
        manager.longs.update(singleBid(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargestOrderFromLongOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.longs.update(singleBid(10, 2));
        manager.longs.update(singleBid(13, 4));
        manager.longs.update(singleBid(15, 2));
        manager.longs.update(singleBid(18, 4));
        manager.longs.update(singleBid(20, 3));

        assert manager.findLargestOrder(10, 20).is(18, 4);
    }

    @Test
    void findLargestOrderFromShortOnly() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(singleAsk(10, 5));
        manager.shorts.update(singleAsk(13, 1));
        manager.shorts.update(singleAsk(15, 2));
        manager.shorts.update(singleAsk(18, 4));
        manager.shorts.update(singleAsk(20, 3));

        assert manager.findLargestOrder(10, 20).is(10, 5);
    }

    @Test
    void findLargestOrderFromShortOnlyWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(singleAsk(10, 2));
        manager.shorts.update(singleAsk(13, 4));
        manager.shorts.update(singleAsk(15, 2));
        manager.shorts.update(singleAsk(18, 4));
        manager.shorts.update(singleAsk(20, 3));

        assert manager.findLargestOrder(10, 20).is(13, 4);
    }

    @Test
    void findLargestOrder() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(singleAsk(20, 3));
        manager.shorts.update(singleAsk(19, 1));
        manager.shorts.update(singleAsk(18, 2));
        manager.shorts.update(singleAsk(17, 8));
        manager.shorts.update(singleAsk(16, 3));
        manager.longs.update(singleBid(15, 2));
        manager.longs.update(singleBid(14, 1));
        manager.longs.update(singleBid(13, 5));
        manager.longs.update(singleBid(12, 4));
        manager.longs.update(singleBid(10, 3));

        assert manager.findLargestOrder(10, 20).is(17, 8);
    }

    @Test
    void findLargestOrderWithMultipleLargest() {
        OrderBookManager manager = new OrderBookManager(service);
        manager.shorts.update(singleAsk(20, 3));
        manager.shorts.update(singleAsk(19, 1));
        manager.shorts.update(singleAsk(18, 2));
        manager.shorts.update(singleAsk(17, 8));
        manager.shorts.update(singleAsk(16, 3));
        manager.longs.update(singleBid(15, 2));
        manager.longs.update(singleBid(14, 1));
        manager.longs.update(singleBid(13, 5));
        manager.longs.update(singleBid(12, 8));
        manager.longs.update(singleBid(10, 3));

        assert manager.findLargestOrder(10, 20).is(12, 8);
    }
}