/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.junit.Test;

import cointoss.Order;
import cointoss.Side;

/**
 * @version 2017/07/26 15:22:24
 */
public class MarketStaticticsTest {

    @Test
    public void market() throws Exception {
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
    public void longOnly() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestSuccessfully(Order.limitLong(1, 10));
        market.execute(Side.BUY, 1, 9);

        assert market.getBase().is(90);
        assert market.getTarget().is(1);
        assert market.calculateProfit().is(0);
    }

    @Test
    public void longMultiple() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));

        assert market.getBase().is(70);
        assert market.getTarget().is(2);
        assert market.calculateProfit().is(10);
    }

    @Test
    public void longDown() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));
        market.execute(Side.BUY, 1, 5);

        assert market.getBase().is(70);
        assert market.getTarget().is(2);
        assert market.calculateProfit().is(-20);
    }

    @Test
    public void shortMultiple() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(1, 10));
        market.requestAndExecution(Order.limitShort(1, 20));

        assert market.getBase().is(130);
        assert market.getTarget().is(-2);
        assert market.calculateProfit().is(-10);
    }

    @Test
    public void shortLong() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(1, 10));
        market.requestAndExecution(Order.limitLong(1, 20));

        assert market.getBase().is(90);
        assert market.getTarget().is(0);
        assert market.calculateProfit().is(-10);
    }

    @Test
    public void longShort() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        market.requestAndExecution(Order.limitShort(1, 20));

        assert market.getBase().is(110);
        assert market.getTarget().is(0);
        assert market.calculateProfit().is(10);
    }
}
