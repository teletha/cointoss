/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.backtest;

import org.junit.jupiter.api.Test;

import cointoss.Side;
import cointoss.backtest.TestableMarket;
import cointoss.order.Order;

/**
 * @version 2018/04/02 16:47:46
 */
class MarketStaticticsTest {

    @Test
    void market() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));

        assert market.getBase().is(90);
        assert market.getTarget().is(1);
        assert market.calculateProfit().is(0);

        market.requestAndExecution(Order.limitShort(1, 15));
        assert market.getBase().is(105);
        assert market.getTarget().is(0);
        assert market.calculateProfit().is(5);
    }

    @Test
    void longOnly() {
        TestableMarket market = new TestableMarket();
        market.requestSuccessfully(Order.limitLong(1, 10));
        market.execute(Side.BUY, 1, 9);

        assert market.getBase().is(90);
        assert market.getTarget().is(1);
        assert market.calculateProfit().is(0);
    }

    @Test
    void longMultiple() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));

        assert market.getBase().is(70);
        assert market.getTarget().is(2);
        assert market.calculateProfit().is(10);
    }

    @Test
    void longDown() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));
        market.execute(Side.BUY, 1, 5);

        assert market.getBase().is(70);
        assert market.getTarget().is(2);
        assert market.calculateProfit().is(-20);
    }

    @Test
    void shortMultiple() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(1, 10));
        market.requestAndExecution(Order.limitShort(1, 20));

        assert market.getBase().is(130);
        assert market.getTarget().is(-2);
        assert market.calculateProfit().is(-10);
    }

    @Test
    void shortLong() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));

        assert market.getBase().is(90);
        assert market.getTarget().is(0);
        assert market.calculateProfit().is(-10);
    }

    @Test
    void longShort() {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitShort(1, 20));

        assert market.getBase().is(110);
        assert market.getTarget().is(0);
        assert market.calculateProfit().is(10);
    }
}
