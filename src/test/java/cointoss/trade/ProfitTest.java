/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.trade.extension.PriceType;
import cointoss.trade.extension.SideType;
import cointoss.trade.extension.TradeTest;
import cointoss.util.Num;

class ProfitTest extends TraderTestSupport {

    @TradeTest
    void realizedProfit(SideType side, PriceType price) {
        Scenario s = entry(side, 2, o -> o.make(price.entry));
        assert s.realizedProfit.is(0);

        executeEntryHalf();
        assert s.realizedProfit.is(0);

        executeEntryAll();
        assert s.realizedProfit.is(0);

        exit(o -> o.make(price.exit));
        assert s.realizedProfit.is(0);

        executeExitHalf();
        assert s.realizedProfit.is(1 * price.diff * side.sign);

        executeExitAll();
        assert s.realizedProfit.is(2 * price.diff * side.sign);
    }

    @TradeTest
    void realizedLoss(SideType side) {
        Scenario s = entry(side, 2, o -> o.make(10));
        assert s.realizedProfit.is(0);

        executeEntryHalf();
        assert s.realizedProfit.is(0);

        executeEntryAll();
        assert s.realizedProfit.is(0);

        exit(o -> o.make(5));
        assert s.realizedProfit.is(0);

        executeExitHalf();
        assert s.realizedProfit.is(-1 * 5 * side.sign);

        executeExitAll();
        assert s.realizedProfit.is(-2 * 5 * side.sign);
    }

    @TradeTest
    void unrealizedProfit(SideType type) {
        Scenario s = entry(type, 2, o -> o.make(10));
        assert s.unrealizedProfit(Num.of(15)).is(0);
        assert s.unrealizedProfit(Num.of(10)).is(0);
        assert s.unrealizedProfit(Num.of(5)).is(0);

        executeEntry(1, 10);
        assert s.unrealizedProfit(Num.of(15)).is(1 * 5 * type.sign);
        assert s.unrealizedProfit(Num.of(10)).is(1 * 0 * type.sign);
        assert s.unrealizedProfit(Num.of(5)).is(1 * -5 * type.sign);

        executeEntry(1, 10);
        assert s.unrealizedProfit(Num.of(15)).is(2 * 5 * type.sign);
        assert s.unrealizedProfit(Num.of(10)).is(2 * 0 * type.sign);
        assert s.unrealizedProfit(Num.of(5)).is(2 * -5 * type.sign);

        exit(o -> o.make(20));
        assert s.unrealizedProfit(Num.of(15)).is(2 * 5 * type.sign);
        assert s.unrealizedProfit(Num.of(10)).is(2 * 0 * type.sign);
        assert s.unrealizedProfit(Num.of(5)).is(2 * -5 * type.sign);

        executeExit(1, 20);
        assert s.unrealizedProfit(Num.of(15)).is(1 * 5 * type.sign);
        assert s.unrealizedProfit(Num.of(10)).is(1 * 0 * type.sign);
        assert s.unrealizedProfit(Num.of(5)).is(1 * -5 * type.sign);

        executeExit(1, 20);
        assert s.unrealizedProfit(Num.of(15)).is(0);
        assert s.unrealizedProfit(Num.of(10)).is(0);
        assert s.unrealizedProfit(Num.of(5)).is(0);
    }
}
