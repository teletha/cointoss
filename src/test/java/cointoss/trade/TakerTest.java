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

import org.junit.jupiter.api.Test;

import cointoss.Direction;
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
    void dividedTakerWillUseTheWorstExecutionPrice1(SidePart side) {
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
        market.perform(Execution.with.direction(side, 0.25).price(10));
        market.perform(Execution.with.direction(side, 0.25).price(6));
        market.perform(Execution.with.direction(side, 0.25).price(14));
        market.perform(Execution.with.direction(side, 0.25).price(8));

        Scenario s = latest();
        if (side.isBuy()) {
            assert s.entryPrice.scale(0).is(12);
        } else {
            assert s.entryPrice.scale(0).is(7);
        }
    }

    @TradeTest
    void dividedTakerWillUseTheWorstExecutionPrice2(SidePart side) {
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
        market.perform(Execution.with.direction(side, 0.25).price(10));
        market.perform(Execution.with.direction(side, 0.25).price(12));
        market.perform(Execution.with.direction(side, 0.25).price(14));
        market.perform(Execution.with.direction(side, 0.25).price(16));

        Scenario s = latest();
        if (side.isBuy()) {
            assert s.entryPrice.is(13);
        } else {
            assert s.entryPrice.is(10);
        }
    }

    @TradeTest
    void dividedTakerWillUseTheWorstExecutionPrice3(SidePart side) {
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
        market.perform(Execution.with.direction(side, 0.25).price(10));
        market.perform(Execution.with.direction(side, 0.25).price(8));
        market.perform(Execution.with.direction(side, 0.25).price(6));
        market.perform(Execution.with.direction(side, 0.25).price(4));

        Scenario s = latest();
        if (side.isBuy()) {
            assert s.entryPrice.is(10);
        } else {
            assert s.entryPrice.is(7);
        }
    }

    @TradeTest
    void takerWillUseTheWorstLatestPrice1(SidePart side) {
        // set latest price
        market.perform(Execution.with.direction(side, 1).price(5));

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
        if (side.isBuy()) {
            assert s.entryPrice.is(10);
        } else {
            assert s.entryPrice.is(5);
        }
    }

    @TradeTest
    void takerWillUseTheWorstLatestPrice2(SidePart side) {
        // set latest price
        market.perform(Execution.with.direction(side, 1).price(10));

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
        market.perform(Execution.with.direction(side, 1).price(5));

        Scenario s = latest();
        if (side.isBuy()) {
            assert s.entryPrice.is(10);
        } else {
            assert s.entryPrice.is(5);
        }
    }

    @Test
    void takerHasHighPriority() {
        when(now(), () -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1, o -> o.make(10));
            }

            @Override
            protected void exit() {
                exitAt(15);
                exitAt(5, o -> o.take());
            }
        });

        // entry
        market.perform(Execution.with.buy(1).price(5));
        awaitOrderBufferingTime();

        // trigger taker exit
        market.perform(Execution.with.buy(1).price(5));
        awaitOrderBufferingTime();

        Scenario s = latest();
        assert s.exitExecutedSize.is(0);

        // taker has high priority
        market.perform(Execution.with.buy(1).price(16));
        assert s.exitExecutedSize.is(1);
        assert s.exitPrice.is(5);
    }
}