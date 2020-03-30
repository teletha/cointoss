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

import cointoss.execution.Execution;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.TradeTest;

class TakerTest extends TraderTestSupport {

    @TradeTest
    void sameSide(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side, 1).price(10));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @TradeTest
    void diffSide(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side.inverse(), 1).price(10));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @TradeTest
    void dividedUpperPrice(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side, 0.5).price(10));
        market.perform(Execution.with.direction(side, 0.5).price(14));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        if (side.isBuy()) {
            assert s.entryPrice.is(12);
        } else {
            assert s.entryPrice.is(10);
        }
    }

    @TradeTest
    void dividedUpperPriceWithDiffSide(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side.inverse(), 0.5).price(10));
        market.perform(Execution.with.direction(side.inverse(), 0.5).price(14));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        if (side.isBuy()) {
            assert s.entryPrice.is(12);
        } else {
            assert s.entryPrice.is(10);
        }
    }

    @TradeTest
    void dividedLowerPrice(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side, 0.5).price(10));
        market.perform(Execution.with.direction(side, 0.25).price(6));
        market.perform(Execution.with.direction(side, 0.25).price(14));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        if (side.isBuy()) {
            assert s.entryPrice.is(10);
        } else {
            assert s.entryPrice.is(8);
        }
    }

    @TradeTest
    void dividedLowerPriceWithDiffSide(SidePart side) {
        when(now(), () -> new Scenario() {

            @Override
            protected void entry() {
                entry(side, 1, o -> o.take());
            }

            @Override
            protected void exit() {
            }
        });

        // trigger taker
        market.perform(Execution.with.direction(side.inverse(), 0.5).price(10));
        market.perform(Execution.with.direction(side.inverse(), 0.5).price(6));

        Scenario s = latest();
        assert s.direction() == side.direction();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        if (side.isBuy()) {
            assert s.entryPrice.is(10);
        } else {
            assert s.entryPrice.is(8);
        }
    }
}
