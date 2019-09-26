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

import antibug.powerassert.PowerAssertOff;
import cointoss.Direction;
import cointoss.execution.Execution;
import kiss.Variable;

class TraderTest extends TraderTestSupport {

    @Test
    void entryBuy() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(1, s -> s.make(10));
                }
            };
        });

        Trade entry = latest();
        assert entry != null;
        assert entry.isBuy();
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(0);
        assert entry.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);
    }

    @Test
    void entrySell() {
        when(now(), v -> {
            return new Trade(Direction.SELL) {

                @Override
                protected void entry() {
                    entry(1, s -> s.make(10));
                }
            };
        });

        Trade entry = latest();
        assert entry != null;
        assert entry.isSell();
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(0);
        assert entry.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(1).price(11));
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(1, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        Trade entry = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);

        // exit entry
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(0);
        assert entry.exitPrice.is(0);

        // don't execute exit entry
        market.perform(Execution.with.buy(1).price(15));
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(0);
        assert entry.exitPrice.is(0);

        // execute exit entry
        market.perform(Execution.with.buy(1).price(21));
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(1);
        assert entry.exitPrice.is(20);
    }

    @Test
    void exitTake() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(1, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20, s -> s.take());
                }
            };
        });

        Trade entry = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        assert entry.entrySize.is(1);
        assert entry.entryExecutedSize.is(1);
        assert entry.entryPrice.is(10);

        // activate exit entry
        market.perform(Execution.with.buy(1).price(20));

        // execute exit entry
        market.perform(Execution.with.buy(1).price(22));
        assert entry.exitSize.is(1);
        assert entry.exitExecutedSize.is(1);
        assert entry.exitPrice.is(22);
    }

    @Test
    void exitWillStopAllEntries() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(3, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        Trade e = latest();

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert e.isEntryTerminated() == false;
        assert e.isExitTerminated() == false;

        // exit pertially
        market.perform(Execution.with.buy(1).price(21));
        assert e.isEntryTerminated();
        assert e.isExitTerminated() == false;

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert e.isEntryTerminated();
        assert e.isExitTerminated();
    }

    @Test
    void profitBuy() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(3, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        Trade e = latest();
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert e.profit(market.latestPrice()).is(10);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(21));
        assert e.profit(market.latestPrice()).is(20);
        assert e.realizedProfit.is(10);
        assert e.unrealizedProfit(market.latestPrice()).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert e.profit(market.latestPrice()).is(20);
        assert e.realizedProfit.is(20);
        assert e.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    @PowerAssertOff
    void profitSell() {
        when(now(), v -> {
            return new Trade(Direction.SELL) {

                @Override
                protected void entry() {
                    entry(3, s -> s.make(20));
                }

                @Override
                protected void exit() {
                    exitAt(10);
                }
            };
        });

        Trade e = latest();
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(21));
        market.elapse(1, SECONDS);
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert e.profit(market.latestPrice()).is(10);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(9));
        assert e.profit(market.latestPrice()).is(20);
        assert e.realizedProfit.is(10);
        assert e.unrealizedProfit(market.latestPrice()).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(9));
        assert e.profit(market.latestPrice()).is(20);
        assert e.realizedProfit.is(20);
        assert e.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    void lossBuy() {
        when(now(), v -> {
            return new Trade(Direction.BUY) {

                @Override
                protected void entry() {
                    entry(2, s -> s.make(20));
                }

                @Override
                protected void exit() {
                    exitAt(10);
                }
            };
        });

        Trade e = latest();
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(19));
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert e.profit(market.latestPrice()).is(-10);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(10));

        // exit partially
        market.perform(Execution.with.buy(1).price(10));
        assert e.profit(market.latestPrice()).is(-20);
        assert e.realizedProfit.is(-10);
        assert e.unrealizedProfit(market.latestPrice()).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(10));
        assert e.profit(market.latestPrice()).is(-20);
        assert e.realizedProfit.is(-20);
        assert e.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    void lossSell() {
        when(now(), v -> {
            return new Trade(Direction.SELL) {

                @Override
                protected void entry() {
                    entry(2, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        Trade e = latest();
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(11));
        assert e.profit(market.latestPrice()).is(0);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert e.profit(market.latestPrice()).is(-10);
        assert e.realizedProfit.is(0);
        assert e.unrealizedProfit(market.latestPrice()).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(20));

        // exit partially
        market.perform(Execution.with.buy(1).price(20));
        assert e.profit(market.latestPrice()).is(-20);
        assert e.realizedProfit.is(-10);
        assert e.unrealizedProfit(market.latestPrice()).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(20));
        assert e.profit(market.latestPrice()).is(-20);
        assert e.realizedProfit.is(-20);
        assert e.unrealizedProfit(market.latestPrice()).is(0);
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
}
