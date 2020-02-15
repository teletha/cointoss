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
import cointoss.util.Num;

class ProfitTest extends TraderTestSupport {

    @TradeTest
    void realizedProfit(TradeSide side) {
        Scenario s = entry(side, 2, o -> o.make(10));
        assert s.realizedProfit.is(0);

        executeEntry(1, 10);
        assert s.realizedProfit.is(0);

        executeEntry(1, 10);
        assert s.realizedProfit.is(0);

        exit(o -> o.make(20));
        assert s.realizedProfit.is(0);

        executeExit(1, 20);
        assert s.realizedProfit.is(1 * 10 * side.sign);

        executeExit(1, 20);
        assert s.realizedProfit.is(2 * 10 * side.sign);
    }

    @TradeTest
    void unrealizedProfit(TradeSide side) {
        Scenario s = entry(side, 2, o -> o.make(10));
        assert s.unrealizedProfit(Num.of(15)).is(0);
        assert s.unrealizedProfit(Num.of(10)).is(0);
        assert s.unrealizedProfit(Num.of(5)).is(0);

        executeEntry(1, 10);
        assert s.unrealizedProfit(Num.of(15)).is(1 * 5 * side.sign);
        assert s.unrealizedProfit(Num.of(10)).is(1 * 0 * side.sign);
        assert s.unrealizedProfit(Num.of(5)).is(1 * -5 * side.sign);

        executeEntry(1, 10);
        assert s.unrealizedProfit(Num.of(15)).is(2 * 5 * side.sign);
        assert s.unrealizedProfit(Num.of(10)).is(2 * 0 * side.sign);
        assert s.unrealizedProfit(Num.of(5)).is(2 * -5 * side.sign);

        exit(o -> o.make(20));
        assert s.unrealizedProfit(Num.of(15)).is(2 * 5 * side.sign);
        assert s.unrealizedProfit(Num.of(10)).is(2 * 0 * side.sign);
        assert s.unrealizedProfit(Num.of(5)).is(2 * -5 * side.sign);

        executeExit(1, 20);
        assert s.unrealizedProfit(Num.of(15)).is(1 * 5 * side.sign);
        assert s.unrealizedProfit(Num.of(10)).is(1 * 0 * side.sign);
        assert s.unrealizedProfit(Num.of(5)).is(1 * -5 * side.sign);

        executeExit(1, 20);
        assert s.unrealizedProfit(Num.of(15)).is(0);
        assert s.unrealizedProfit(Num.of(10)).is(0);
        assert s.unrealizedProfit(Num.of(5)).is(0);
    }
}
