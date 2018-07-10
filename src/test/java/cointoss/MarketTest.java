/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.backtest.TestableMarket;
import cointoss.order.Order;
import cointoss.util.Num;

/**
 * @version 2018/07/10 21:56:26
 */
class MarketTest {

    TestableMarket market;

    @BeforeEach
    void init() {
        market = new TestableMarket();
    }

    @Test
    void baseCurrency() {
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(100));

        // non-matching order
        market.execute(buy(1, 10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(100));

        // matching order
        market.requestAndExecution(Order.limitLong(1, 10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(90));
    }
}
