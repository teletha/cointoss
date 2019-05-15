/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.order.Order;

/**
 * @version 2018/07/10 23:17:09
 */
class MarketStaticticsTest {

    @Test
    void market() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.buy(1).price(10));

        assert market.baseCurrency.v.is(90);
        assert market.targetCurrency.v.is(1);
        assert market.calculateProfit().is(0);

        market.requestAndExecution(Order.with.sell(1).price(15));
        assert market.baseCurrency.v.is(105);
        assert market.targetCurrency.v.is(0);
        assert market.calculateProfit().is(5);
    }

    @Test
    void longOnly() {
        VerifiableMarket market = new VerifiableMarket();
        market.request(Order.with.buy(1).price(10)).to();
        market.perform(Execution.with.buy(1).price(9));

        assert market.baseCurrency.v.is(90);
        assert market.targetCurrency.v.is(1);
        assert market.calculateProfit().is(0);
    }

    @Test
    void longMultiple() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.buy(1).price(10));
        market.requestAndExecution(Order.with.buy(1).price(20));

        assert market.baseCurrency.v.is(70);
        assert market.targetCurrency.v.is(2);
        assert market.calculateProfit().is(10);
    }

    @Test
    void longDown() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.buy(1).price(10));
        market.requestAndExecution(Order.with.buy(1).price(20));
        market.perform(Execution.with.buy(1).price(5));

        assert market.baseCurrency.v.is(70);
        assert market.targetCurrency.v.is(2);
        assert market.calculateProfit().is(-20);
    }

    @Test
    void shortMultiple() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.sell(1).price(10));
        market.requestAndExecution(Order.with.sell(1).price(20));

        assert market.baseCurrency.v.is(130);
        assert market.targetCurrency.v.is(-2);
        assert market.calculateProfit().is(-10);
    }

    @Test
    void shortLong() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.sell(1).price(10));
        market.requestAndExecution(Order.with.buy(1).price(20));

        assert market.baseCurrency.v.is(90);
        assert market.targetCurrency.v.is(0);
        assert market.calculateProfit().is(-10);
    }

    @Test
    void longShort() {
        VerifiableMarket market = new VerifiableMarket();
        market.requestAndExecution(Order.with.buy(1).price(10));
        market.requestAndExecution(Order.with.sell(1).price(20));

        assert market.baseCurrency.v.is(110);
        assert market.targetCurrency.v.is(0);
        assert market.calculateProfit().is(10);
    }
}
