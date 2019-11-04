/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import static java.time.temporal.ChronoUnit.SECONDS;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;

class TraderTest extends TraderTestSupport {

    @Test
    void entryBuy() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario != null;
        assert scenario.isBuy();
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0);
        assert scenario.entryPrice.is(0);

        // execute entry
        verifiable.market.perform(Execution.with.buy(1).price(9));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);
    }

    @Test
    void entrySell() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.SELL, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario != null;
        assert scenario.isSell();
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0);
        assert scenario.entryPrice.is(0);

        // execute entry
        verifiable.market.perform(Execution.with.buy(1).price(11));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        Scenario scenario = verifiable.verify();

        // execute entry
        verifiable.market.perform(Execution.with.buy(1).price(9));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);

        // exit entry
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0);
        assert scenario.exitPrice.is(0);

        // don't execute exit entry
        verifiable.market.perform(Execution.with.buy(1).price(15));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0);
        assert scenario.exitPrice.is(0);

        // execute exit entry
        verifiable.market.perform(Execution.with.buy(1).price(21));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
        assert scenario.exitPrice.is(20);
    }

    @Test
    void exitTake() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20, s -> s.take());
            }
        };

        Scenario scenario = verifiable.verify();

        // execute entry
        verifiable.market.perform(Execution.with.buy(1).price(9));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);

        // activate exit entry
        verifiable.market.perform(Execution.with.buy(1).price(20));

        // execute exit entry
        verifiable.market.perform(Execution.with.buy(1).price(22));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
        assert scenario.exitPrice.is(22);
    }

    @Test
    void exitWillStopAllEntries() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 3, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        Scenario scenario = verifiable.verify();

        // entry partially
        verifiable.market.perform(Execution.with.buy(2).price(9));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.isEntryTerminated() == false;
        assert scenario.isExitTerminated() == false;

        // exit pertially
        verifiable.market.perform(Execution.with.buy(1).price(21));
        assert scenario.isEntryTerminated();
        assert scenario.isExitTerminated() == false;

        // exit all
        verifiable.market.perform(Execution.with.buy(1).price(21));
        assert scenario.isEntryTerminated();
        assert scenario.isExitTerminated();
    }

    @Test
    void profitBuy() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 3, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // entry partially
        verifiable.market.perform(Execution.with.buy(2).price(9));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // execute profit
        verifiable.market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(verifiable.market.latestPrice()).is(10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(10);

        // exit partially
        verifiable.market.perform(Execution.with.buy(1).price(21));
        assert scenario.profit(verifiable.market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(10);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(10);

        // exit all
        verifiable.market.perform(Execution.with.buy(1).price(21));
        assert scenario.profit(verifiable.market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(20);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);
    }

    @Test
    void profitSell() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.SELL, 3, s -> s.make(20));
                });
            }

            @Override
            protected void exit() {
                exitAt(10);
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // entry partially
        verifiable.market.perform(Execution.with.buy(2).price(21));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // execute profit
        verifiable.market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(verifiable.market.latestPrice()).is(10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(10);

        // exit partially
        verifiable.market.perform(Execution.with.buy(1).price(9));
        assert scenario.profit(verifiable.market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(10);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(10);

        // exit all
        verifiable.market.perform(Execution.with.buy(1).price(9));
        assert scenario.profit(verifiable.market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(20);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);
    }

    @Test
    void lossBuy() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 2, s -> s.make(20));
                });
            }

            @Override
            protected void exit() {
                exitAt(10);
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // entry partially
        verifiable.market.perform(Execution.with.buy(2).price(19));
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // execute loss
        verifiable.market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(verifiable.market.latestPrice()).is(-10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(-10);

        // activate stop loss
        verifiable.market.perform(Execution.with.buy(1).price(10));

        // exit partially
        verifiable.market.perform(Execution.with.buy(1).price(10));
        assert scenario.profit(verifiable.market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-10);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(-10);

        // exit all
        verifiable.market.perform(Execution.with.buy(1).price(10));
        assert scenario.profit(verifiable.market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-20);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);
    }

    @Test
    void lossSell() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.SELL, 2, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // entry partially
        verifiable.market.perform(Execution.with.buy(2).price(11));
        assert scenario.profit(verifiable.market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);

        // execute loss
        verifiable.market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(verifiable.market.latestPrice()).is(-10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(-10);

        // activate stop loss
        verifiable.market.perform(Execution.with.buy(1).price(20));

        // exit partially
        verifiable.market.perform(Execution.with.buy(1).price(20));
        assert scenario.profit(verifiable.market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-10);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(-10);

        // exit all
        verifiable.market.perform(Execution.with.buy(1).price(20));
        assert scenario.profit(verifiable.market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-20);
        assert scenario.unrealizedProfit(verifiable.market.latestPrice()).is(0);
    }

    // @Test
    // void keep() {
    // Variable<Execution> state = verifiable.market.timeline.take(keep(5, SECONDS, e ->
    // e.price.isLessThan(10))).to();
    // assert state.isAbsent();
    //
    // // keep more than 10
    // verifiable.market.perform(Execution.with.buy(1).price(15), 2);
    // verifiable.market.perform(Execution.with.buy(1).price(15), 2);
    // verifiable.market.perform(Execution.with.buy(1).price(15), 2);
    // assert state.isAbsent();
    //
    // // keep less than 10 during 3 seconds
    // verifiable.market.perform(Execution.with.buy(1).price(9), 1);
    // verifiable.market.perform(Execution.with.buy(1).price(9), 1);
    // verifiable.market.perform(Execution.with.buy(1).price(15), 3);
    // verifiable.market.perform(Execution.with.buy(1).price(15), 3);
    // assert state.isAbsent();
    //
    // // keep less than 10 during 5 seconds
    // verifiable.market.perform(Execution.with.buy(1).price(9), 3);
    // verifiable.market.perform(Execution.with.buy(1).price(9), 3);
    // verifiable.market.perform(Execution.with.buy(1).price(9), 3);
    // assert state.isPresent();
    // }
}
