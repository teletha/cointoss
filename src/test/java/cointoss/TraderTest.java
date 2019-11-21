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

import static java.time.temporal.ChronoUnit.SECONDS;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import kiss.I;
import kiss.Variable;

class TraderTest extends TraderTestSupport {

    @Test
    void entryBuy() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        Scenario s = latest();
        assert s.isBuy();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0);
        assert s.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.5);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @Test
    void entrySell() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        Scenario s = latest();
        assert s.isSell();
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0);
        assert s.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(11));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.5);
        assert s.entryPrice.is(10);

        // execute entry
        market.perform(Execution.with.buy(0.5).price(11));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Scenario s = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.entryPrice.is(10);

        // exit entry
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
        assert s.exitPrice.is(0);

        // don't execute exit entry
        market.perform(Execution.with.buy(1).price(15));
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
        assert s.exitPrice.is(0);

        // execute exit entry
        market.perform(Execution.with.buy(1).price(21));
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(1);
        assert s.exitPrice.is(20);
    }

    @Test
    void exitTake() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20, s -> s.take());
            }
        });

        Scenario scenario = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);

        // activate exit entry
        market.perform(Execution.with.buy(1).price(20));
        awaitOrderBufferingTime();

        // execute exit entry
        market.perform(Execution.with.buy(1).price(22));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
        assert scenario.exitPrice.is(22);
    }

    @Test
    void exitWillStopAllEntries() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 3, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Scenario s = latest();

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert s.isEntryTerminated() == false;
        assert s.isExitTerminated() == false;

        // exit pertially
        market.perform(Execution.with.buy(1).price(21));
        assert s.isEntryTerminated();
        assert s.isExitTerminated() == false;

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert s.isEntryTerminated();
        assert s.isExitTerminated();
    }

    @Test
    void profitBuy() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 3, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Scenario s = latest();
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert s.profit(market.tickers.latestPrice.v).is(10);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(21));
        assert s.profit(market.tickers.latestPrice.v).is(20);
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert s.profit(market.tickers.latestPrice.v).is(20);
        assert s.realizedProfit.is(20);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);
    }

    @Test
    void profitSell() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, 3, s -> s.make(20));
            }

            @Override
            protected void exit() {
                exitAt(10);
            }
        });

        Scenario s = latest();
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(21));
        market.elapse(1, SECONDS);
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert s.profit(market.tickers.latestPrice.v).is(10);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(9));
        assert s.profit(market.tickers.latestPrice.v).is(20);
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(9));
        assert s.profit(market.tickers.latestPrice.v).is(20);
        assert s.realizedProfit.is(20);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);
    }

    @Test
    void lossBuy() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 2, s -> s.make(20));
            }

            @Override
            protected void exit() {
                exitAt(10);
            }
        });

        Scenario s = latest();
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(19));
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert s.profit(market.tickers.latestPrice.v).is(-10);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(10));

        // exit partially
        market.perform(Execution.with.buy(1).price(10));
        assert s.profit(market.tickers.latestPrice.v).is(-20);
        assert s.realizedProfit.is(-10);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(10));
        assert s.profit(market.tickers.latestPrice.v).is(-20);
        assert s.realizedProfit.is(-20);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);
    }

    @Test
    void lossSell() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.SELL, 2, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        });

        Scenario s = latest();
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(11));
        assert s.profit(market.tickers.latestPrice.v).is(0);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert s.profit(market.tickers.latestPrice.v).is(-10);
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(20));

        // exit partially
        market.perform(Execution.with.buy(1).price(20));
        assert s.profit(market.tickers.latestPrice.v).is(-20);
        assert s.realizedProfit.is(-10);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(20));
        assert s.profit(market.tickers.latestPrice.v).is(-20);
        assert s.realizedProfit.is(-20);
        assert s.unrealizedProfit(market.tickers.latestPrice.v).is(0);
    }

    @Test
    void keep() {
        Variable<Execution> state = market.timeline.take(keep(5, SECONDS, e -> e.price.isLessThan(10))).to();
        assert state.isAbsent();

        // keep more than 10
        market.perform(Execution.with.buy(1).price(15), 2);
        market.perform(Execution.with.buy(1).price(15), 2);
        market.perform(Execution.with.buy(1).price(15), 2);
        assert state.isAbsent();

        // keep less than 10 during 3 seconds
        market.perform(Execution.with.buy(1).price(9), 1);
        market.perform(Execution.with.buy(1).price(9), 1);
        market.perform(Execution.with.buy(1).price(15), 3);
        market.perform(Execution.with.buy(1).price(15), 3);
        assert state.isAbsent();

        // keep less than 10 during 5 seconds
        market.perform(Execution.with.buy(1).price(9), 3);
        market.perform(Execution.with.buy(1).price(9), 3);
        market.perform(Execution.with.buy(1).price(9), 3);
        assert state.isPresent();
    }

    @Test
    void holdSizeAndHoldMaxSize() {
        entry(Execution.with.buy(1).price(10));
        assert holdSize().is(1);
        assert holdMaxSize.is(1);

        entry(Execution.with.buy(2).price(10));
        assert holdSize().is(3);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(1).price(20));
        assert holdSize().is(2);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(3).price(20));
        assert holdSize().is(-1);
        assert holdMaxSize.is(3);

        entry(Execution.with.sell(3).price(20));
        assert holdSize().is(-4);
        assert holdMaxSize.is(4);
    }

    @Test
    void disableAlways() {
        disableWhile(I.signal(true));

        // don't executed because disabled
        entry(Execution.with.buy(1).price(10));

        Scenario s = latest();
        assert s == null;
    }

    @Test
    void enableWhile() {
        Variable<Boolean> disable = Variable.of(false);
        disableWhile(disable.observe());

        // enable
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.sell(1).price(20));
        Scenario s = latest();
        assert s.entrySize.is(1);
        assert s.exitSize.is(1);

        // disable
        disable.set(true);
        entryAndExit(Execution.with.buy(1).price(20), Execution.with.sell(1).price(30));
        Scenario previous = latest();
        assert previous == s;

        // enable
        disable.set(false);
        entryAndExit(Execution.with.buy(2).price(30), Execution.with.sell(2).price(40));
        s = latest();
        assert previous != s;
        assert s.entrySize.is(2);
        assert s.exitSize.is(2);
    }
}
