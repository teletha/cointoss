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

/**
 * @version 2017/08/24 1:43:21
 */
public class TradeTest {

    @Test
    public void buy() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(1, 10));
        assert market.remaining.is(1);
        assert market.price.is(10);
        assert market.position.isBuy();

        market.requestAndExecution(Order.limitLong(1, 20));
        assert market.remaining.is(2);
        assert market.price.is(15);
        assert market.position.isBuy();
    }

    @Test
    public void buyAndSell() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isBuy();

        market.requestAndExecution(Order.limitShort(2, 20));
        assert market.remaining.is(0);
        assert market.price.is(0);
        assert market.position == null;
    }

    @Test
    public void buyAndUnderflowSell() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isBuy();

        market.requestAndExecution(Order.limitShort(1, 20));
        assert market.remaining.is(1);
        assert market.price.is(10);
        assert market.position.isBuy();
    }

    @Test
    public void buyAndOverflowSell() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitLong(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isBuy();

        market.requestAndExecution(Order.limitShort(3, 20));
        assert market.remaining.is(1);
        assert market.price.is(20);
        assert market.position.isSell();
    }

    @Test
    public void sell() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(1, 10));
        assert market.remaining.is(1);
        assert market.price.is(10);
        assert market.position.isSell();

        market.requestAndExecution(Order.limitShort(1, 20));
        assert market.remaining.is(2);
        assert market.price.is(15);
        assert market.position.isSell();
    }

    @Test
    public void sellAndBuy() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isSell();

        market.requestAndExecution(Order.limitLong(2, 20));
        assert market.remaining.is(0);
        assert market.price.is(0);
        assert market.position == null;
    }

    @Test
    public void sellAndUnderflowBuy() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isSell();

        market.requestAndExecution(Order.limitLong(1, 20));
        assert market.remaining.is(1);
        assert market.price.is(10);
        assert market.position.isSell();
    }

    @Test
    public void sellAndOverflowBuy() throws Exception {
        TestableMarket market = new TestableMarket();
        market.requestAndExecution(Order.limitShort(2, 10));
        assert market.remaining.is(2);
        assert market.price.is(10);
        assert market.position.isSell();

        market.requestAndExecution(Order.limitLong(3, 20));
        assert market.remaining.is(1);
        assert market.price.is(20);
        assert market.position.isBuy();
    }
}
