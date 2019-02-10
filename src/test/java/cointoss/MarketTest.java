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

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cointoss.order.Order;
import cointoss.util.Num;

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
        market.execute(buy(1, 10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(100));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(0));

        // matching order
        market.requestAndExecution(Order.limitLong(1, 10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(90));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(1));

        market.requestAndExecution(Order.limitLong(2, 20));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(50));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(3));

        // exit
        market.requestAndExecution(Order.limitShort(1, 20));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(70));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(2));

        market.requestAndExecution(Order.limitShort(2, 10));
        assert market.initialBaseCurrency.is(100);
        assert market.baseCurrency.is(Num.of(90));
        assert market.initialTargetCurrency.is(0);
        assert market.targetCurrency.is(Num.of(0));
    }
}
