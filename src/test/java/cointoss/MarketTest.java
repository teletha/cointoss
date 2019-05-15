/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.util.Num;
import cointoss.verify.VerifiableMarket;

/**
 * @version 2018/07/11 13:27:20
 */
class MarketTest {

    VerifiableMarket market;

    @BeforeEach
    void init() {
        market = new VerifiableMarket();
    }

    @Test
    void balance() {
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(100));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(0));

        // non-matching order
        market.perform(Execution.with.buy(1).price(10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(100));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(0));

        // matching order
        market.requestAndExecution(Order.with.buy(1).price(10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(90));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(1));

        market.requestAndExecution(Order.with.buy(2).price(20));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(50));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(3));

        // exit
        market.requestAndExecution(Order.with.sell(1).price(20));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(70));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(2));

        market.requestAndExecution(Order.with.sell(2).price(10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(90));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(0));
    }
}
