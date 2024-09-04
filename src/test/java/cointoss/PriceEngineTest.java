/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import hypatia.Num;

public class PriceEngineTest {

    private TestableMarket market = new TestableMarket();

    @Test
    void buy() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.BUY, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(10));
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(1));
        assert counter.get() == 1;
    }

    @Test
    void buyMultiPrices() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.BUY, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(10), Direction.BUY, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(15));
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(10));
        assert counter.get() == 1;

        market.perform(Execution.with.buy(1).price(5));
        assert counter.get() == 2;
    }

    @Test
    void buyMultiPricesAtSameTiming() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.BUY, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(10), Direction.BUY, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(5));
        assert counter.get() == 2;
    }

    @Test
    void buySamePrice() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.BUY, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(5), Direction.BUY, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(5));
        assert counter.get() == 2;
    }

    @Test
    void sell() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.SELL, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(1));
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(10));
        assert counter.get() == 1;
    }

    @Test
    void sellMultiPrices() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.SELL, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(10), Direction.SELL, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(1));
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(5));
        assert counter.get() == 1;

        market.perform(Execution.with.buy(1).price(10));
        assert counter.get() == 2;
    }

    @Test
    void sellMultiPricesAtSameTiming() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.SELL, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(10), Direction.SELL, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(15));
        assert counter.get() == 2;
    }

    @Test
    void sellSamePrice() {
        AtomicInteger counter = new AtomicInteger();

        market.priceMatcher.register(Num.of(5), Direction.SELL, counter::incrementAndGet);
        market.priceMatcher.register(Num.of(5), Direction.SELL, counter::incrementAndGet);
        assert counter.get() == 0;

        market.perform(Execution.with.buy(1).price(5));
        assert counter.get() == 2;
    }
}
