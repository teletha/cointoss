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
import kiss.Variable;

class TraderTest extends TraderTestSupport {

    @Test
    void entryBuy() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 1, s -> s.make(10));
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario != null;
        assert scenario.isBuy();
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0);
        assert scenario.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);
    }

    @Test
    void entrySell() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.SELL, 1, s -> s.make(10));
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario != null;
        assert scenario.isSell();
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0);
        assert scenario.entryPrice.is(0);

        // execute entry
        market.perform(Execution.with.buy(1).price(11));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);
    }

    @Test
    void exitMakeAtPrice() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 1, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        TradingScenario scenario = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);

        // exit entry
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0);
        assert scenario.exitPrice.is(0);

        // don't execute exit entry
        market.perform(Execution.with.buy(1).price(15));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0);
        assert scenario.exitPrice.is(0);

        // execute exit entry
        market.perform(Execution.with.buy(1).price(21));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
        assert scenario.exitPrice.is(20);
    }

    @Test
    void exitTake() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 1, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20, s -> s.take());
                }
            };
        });

        TradingScenario scenario = latest();

        // execute entry
        market.perform(Execution.with.buy(1).price(9));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.entryPrice.is(10);

        // activate exit entry
        market.perform(Execution.with.buy(1).price(20));

        // execute exit entry
        market.perform(Execution.with.buy(1).price(22));
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
        assert scenario.exitPrice.is(22);
    }

    @Test
    void exitWillStopAllEntries() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 3, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        TradingScenario scenario = latest();

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert scenario.isEntryTerminated() == false;
        assert scenario.isExitTerminated() == false;

        // exit pertially
        market.perform(Execution.with.buy(1).price(21));
        assert scenario.isEntryTerminated();
        assert scenario.isExitTerminated() == false;

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert scenario.isEntryTerminated();
        assert scenario.isExitTerminated();
    }

    @Test
    void profitBuy() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 3, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(9));
        market.elapse(1, SECONDS);
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(market.latestPrice()).is(10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(21));
        assert scenario.profit(market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(10);
        assert scenario.unrealizedProfit(market.latestPrice()).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(21));
        assert scenario.profit(market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(20);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    void profitSell() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.SELL, 3, s -> s.make(20));
                }

                @Override
                protected void exit() {
                    exitAt(10);
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(21));
        market.elapse(1, SECONDS);
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // execute profit
        market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(market.latestPrice()).is(10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(10);

        // exit partially
        market.perform(Execution.with.buy(1).price(9));
        assert scenario.profit(market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(10);
        assert scenario.unrealizedProfit(market.latestPrice()).is(10);

        // exit all
        market.perform(Execution.with.buy(1).price(9));
        assert scenario.profit(market.latestPrice()).is(20);
        assert scenario.realizedProfit.is(20);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    void lossBuy() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.BUY, 2, s -> s.make(20));
                }

                @Override
                protected void exit() {
                    exitAt(10);
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(19));
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(market.latestPrice()).is(-10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(10));

        // exit partially
        market.perform(Execution.with.buy(1).price(10));
        assert scenario.profit(market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-10);
        assert scenario.unrealizedProfit(market.latestPrice()).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(10));
        assert scenario.profit(market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-20);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);
    }

    @Test
    void lossSell() {
        when(now(), v -> {
            return new TradingScenario() {

                @Override
                protected void entry() {
                    entry(Direction.SELL, 2, s -> s.make(10));
                }

                @Override
                protected void exit() {
                    exitAt(20);
                }
            };
        });

        TradingScenario scenario = latest();
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // entry partially
        market.perform(Execution.with.buy(2).price(11));
        assert scenario.profit(market.latestPrice()).is(0);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);

        // execute loss
        market.perform(Execution.with.buy(1).price(15));
        assert scenario.profit(market.latestPrice()).is(-10);
        assert scenario.realizedProfit.is(0);
        assert scenario.unrealizedProfit(market.latestPrice()).is(-10);

        // activate stop loss
        market.perform(Execution.with.buy(1).price(20));

        // exit partially
        market.perform(Execution.with.buy(1).price(20));
        assert scenario.profit(market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-10);
        assert scenario.unrealizedProfit(market.latestPrice()).is(-10);

        // exit all
        market.perform(Execution.with.buy(1).price(20));
        assert scenario.profit(market.latestPrice()).is(-20);
        assert scenario.realizedProfit.is(-20);
        assert scenario.unrealizedProfit(market.latestPrice()).is(0);
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
