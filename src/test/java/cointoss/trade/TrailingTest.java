/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import cointoss.execution.Execution;
import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.TradeTest;
import cointoss.util.Num;

public class TrailingTest extends TraderTestSupport {

    @TradeTest(price = {1000, 1200})
    void losscutAtBottomPrice(SidePart side, PricePart price) {
        Num losscutPriceDistance = price.diffHalfN;
        Num entryTriggerPrice = price.entryN.minus(side, 1);
        Num losscutTriggerPrice = price.entryN.minus(side, losscutPriceDistance);

        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, 1, s -> s.make(price.entry));
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(losscutPriceDistance));
            }
        });

        Scenario s = latest();

        // entry
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // no effect
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.exits.size() == 0;

        // trigger losscut at bottom
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice).date(afterSecond(5)));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(0);

        // perform losscut
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize().is(1);
        assert s.exitPrice.is(losscutTriggerPrice);
    }

    @TradeTest(price = {1000, 1200})
    void losscutAtTrailedPrice(SidePart side, PricePart price) {
        Num losscutPriceDistance = price.diffHalfN;
        Num entryTriggerPrice = price.entryN.minus(side, 1);
        Num highPrice = price.entryN.plus(side, 50);
        Num losscutTriggerPrice = highPrice.minus(side, losscutPriceDistance);
        Num bottomTrailPrice = price.entryN.minus(side, losscutPriceDistance);

        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, 1, s -> s.make(price.entry));
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(losscutPriceDistance));
            }
        });

        Scenario s = latest();

        // entry
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // advance trailing price
        market.perform(Execution.with.buy(1).price(highPrice).date(afterSecond(5)));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // trigger losscut at trailed price
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(0);

        // perform losscut
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize().is(1);
        assert s.exitPrice.is(losscutTriggerPrice);
        assert s.exitPrice.isNot(bottomTrailPrice);
    }

    @TradeTest(price = {1000, 1200})
    void losscutAtMaxTrailedPrice(SidePart side, PricePart price) {
        Num losscutPriceDistance = price.diffHalfN;
        Num entryTriggerPrice = price.entryN.minus(side, 1);
        Num highPrice = price.entryN.plus(side, 100);
        Num losscutTriggerPrice = highPrice.minus(side, losscutPriceDistance);
        Num bottomTrailPrice = price.entryN.minus(side, losscutPriceDistance);

        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, 1, s -> s.make(price.entry));
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(losscutPriceDistance));
            }
        });

        Scenario s = latest();

        // entry
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // advance trailing price
        market.perform(Execution.with.buy(1).price(highPrice).date(afterSecond(5)));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // trigger losscut at trailed price
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(0);

        // perform losscut
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize().is(1);
        assert s.exitPrice.is(losscutTriggerPrice);
        assert s.exitPrice.isNot(bottomTrailPrice);
    }

    @TradeTest(price = {1000, 1200})
    void losscutAtOverTrailedPrice(SidePart side, PricePart price) {
        Num losscutPriceDistance = price.diffHalfN;
        Num entryTriggerPrice = price.entryN.minus(side, 1);
        Num highPrice = price.entryN.plus(side, 500);
        Num losscutTriggerPrice = price.entryN;
        Num bottomTrailPrice = price.entryN.minus(side, losscutPriceDistance);

        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, 1, s -> s.make(price.entry));
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(losscutPriceDistance));
            }
        });

        Scenario s = latest();

        // entry
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // advance trailing price
        market.perform(Execution.with.buy(1).price(highPrice).date(afterSecond(5)));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // trigger losscut at trailed price
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(0);

        // perform losscut
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize().is(1);
        assert s.exitPrice.is(losscutTriggerPrice);
        assert s.exitPrice.isNot(bottomTrailPrice);
    }

    @TradeTest(price = {1000, 1200})
    void losscutAtProfitTrailedPrice(SidePart side, PricePart price) {
        Num losscutPriceDistance = price.diffHalfN;
        Num profitPriceDistance = Num.of(300);
        Num entryTriggerPrice = price.entryN.minus(side, 1);
        Num highPrice = price.entryN.plus(side, 500);
        Num losscutTriggerPrice = price.entryN.plus(side, profitPriceDistance);
        Num bottomTrailPrice = price.entryN.minus(side, losscutPriceDistance);

        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, 1, s -> s.make(price.entry));
            }

            @Override
            protected void exit() {
                exitAt(Trailing.with.losscut(losscutPriceDistance).profit(profitPriceDistance));
            }
        });

        Scenario s = latest();

        // entry
        market.perform(Execution.with.buy(1).price(entryTriggerPrice));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // advance trailing price
        market.perform(Execution.with.buy(1).price(highPrice).date(afterSecond(5)));
        assert s.entries.size() == 1;
        assert s.exits.size() == 0;

        // trigger losscut at trailed price
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize.is(0);

        // perform losscut
        market.perform(Execution.with.buy(1).price(losscutTriggerPrice));
        assert s.exits.size() == 1;
        assert s.exitExecutedSize().is(1);
        assert s.exitPrice.is(losscutTriggerPrice);
        assert s.exitPrice.isNot(bottomTrailPrice);
    }
}