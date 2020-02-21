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

import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;

class ScenarioProfitTest extends TraderTestSupport {

    @TradeTest
    void realizedProfit(SidePart side, SizePart size, PricePart price) {
        Scenario s = entry(side, size, o -> o.make(price.entry));
        assert s.realizedProfit.is(0);

        executeEntryHalf();
        assert s.realizedProfit.is(0);

        executeEntryAll();
        assert s.realizedProfit.is(0);

        exit(o -> o.make(price.exit));
        assert s.realizedProfit.is(0);

        executeExitHalf();
        assert s.realizedProfit.is(size.half * price.diff * side.sign);

        executeExitAll();
        assert s.realizedProfit.is(size.num * price.diff * side.sign);
    }

    @TradeTest
    void unrealizedProfit(SidePart side, SizePart size, PricePart price) {
        Scenario s = entry(side, size, o -> o.make(price.entry));
        assert s.unrealizedProfit(price.middleN).is(0);
        assert s.unrealizedProfit(price.entryN).is(0);
        assert s.unrealizedProfit(price.lossN).is(0);

        executeEntryHalf();
        assert s.unrealizedProfit(price.middleN).is(size.half * price.diffHalf * side.sign);
        assert s.unrealizedProfit(price.entryN).is(size.half * 0 * side.sign);
        assert s.unrealizedProfit(price.lossN).is(size.half * -price.diffHalf * side.sign);

        executeEntryAll();
        assert s.unrealizedProfit(price.middleN).is(size.num * price.diffHalf * side.sign);
        assert s.unrealizedProfit(price.entryN).is(size.num * 0 * side.sign);
        assert s.unrealizedProfit(price.lossN).is(size.num * -price.diffHalf * side.sign);

        exit(o -> o.make(price.exit));
        assert s.unrealizedProfit(price.middleN).is(size.num * price.diffHalf * side.sign);
        assert s.unrealizedProfit(price.entryN).is(size.num * 0 * side.sign);
        assert s.unrealizedProfit(price.lossN).is(size.num * -price.diffHalf * side.sign);

        executeExitHalf();
        assert s.unrealizedProfit(price.middleN).is(size.half * price.diffHalf * side.sign);
        assert s.unrealizedProfit(price.entryN).is(size.half * 0 * side.sign);
        assert s.unrealizedProfit(price.lossN).is(size.half * -price.diffHalf * side.sign);

        executeExitAll();
        assert s.unrealizedProfit(price.middleN).is(0);
        assert s.unrealizedProfit(price.entryN).is(0);
        assert s.unrealizedProfit(price.lossN).is(0);
    }

    @TradeTest
    void profit(SidePart side, SizePart size, PricePart price) {
        Scenario s = entry(side, size, o -> o.make(price.entry));
        assert s.profit(price.middleN).is(0);
        assert s.profit(price.entryN).is(0);
        assert s.profit(price.lossN).is(0);

        executeEntryHalf();
        assert s.profit(price.middleN).is(size.half * price.diffHalf * side.sign);
        assert s.profit(price.entryN).is(size.half * 0 * side.sign);
        assert s.profit(price.lossN).is(size.half * -price.diffHalf * side.sign);

        executeEntryAll();
        assert s.profit(price.middleN).is(size.num * price.diffHalf * side.sign);
        assert s.profit(price.entryN).is(size.num * 0 * side.sign);
        assert s.profit(price.lossN).is(size.num * -price.diffHalf * side.sign);

        exit(o -> o.make(price.exit));
        assert s.profit(price.middleN).is(size.num * price.diffHalf * side.sign);
        assert s.profit(price.entryN).is(size.num * 0 * side.sign);
        assert s.profit(price.lossN).is(size.num * -price.diffHalf * side.sign);

        executeExitHalf();
        assert s.profit(price.middleN).is(size.half * price.diffHalf * side.sign + size.half * price.diff * side.sign);
        assert s.profit(price.entryN).is(size.half * 0 * side.sign + size.half * price.diff * side.sign);
        assert s.profit(price.lossN).is(size.half * -price.diffHalf * side.sign + size.half * price.diff * side.sign);

        executeExitAll();
        assert s.profit(price.middleN).is(size.num * price.diff * side.sign);
        assert s.profit(price.entryN).is(size.num * price.diff * side.sign);
        assert s.profit(price.lossN).is(size.num * price.diff * side.sign);
    }
}
