/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.order.Order;

class EntryTest extends TraderTestSupport {

    @Test
    void holdTime() {
        entryAndExit(Execution.with.buy(1).price(10).date(second(0)), Execution.with.buy(1).price(20).date(second(10)));

        TradingScenario scenario = latest();
        assert scenario.holdTime().equals(Duration.ofSeconds(10));
    }

    @Test
    void isTerminated() {
        entry(Execution.with.buy(1).price(10));

        TradingScenario scenario = latest();
        assert scenario.isTerminated() == false;
    }

    @Test
    void isEntryTerminated() {
        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }
        };

        TradingScenario scenario = latest();
        assert scenario.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert scenario.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert scenario.isEntryTerminated() == true;
    }

    @Test
    void isExitTerminated() {
        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        TradingScenario scenario = latest();
        assert scenario.isExitTerminated() == false;

        market.perform(Execution.with.buy(1).price(9));
        assert scenario.isExitTerminated() == false;

        market.elapse(1, SECONDS);
        market.perform(Execution.with.buy(0.5).price(21));
        assert scenario.isExitTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(21));
        assert scenario.isExitTerminated() == true;
    }

    @Test
    void entryWithMultipleExecutionAndExitAtPrice() {
        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
            }
        };

        TradingScenario scenario = latest();
        assert scenario.exits.size() == 0;

        market.perform(Execution.with.buy(0.1).price(9));
        market.perform(Execution.with.buy(0.2).price(9));
        market.perform(Execution.with.buy(0.3).price(9));
        market.perform(Execution.with.buy(0.4).price(9));
        market.elapse(1, SECONDS);
        assert scenario.exits.size() == 1;
    }

    @Test
    void exitAndStop() {
        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.make(10));
                });
            }

            @Override
            protected void exit() {
                exitAt(20);
                exitAt(5);
            }
        };

        TradingScenario scenario = latest();
        assert scenario.exits.size() == 0;

        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert scenario.exits.size() == 1; // exit is ordered
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.1).price(5)); // trigger stop
        market.perform(Execution.with.buy(0.5).price(5));
        assert scenario.exits.size() == 2; // stop is ordered
        assert scenario.exits.stream().allMatch(Order::isActive);
        assert scenario.isExitTerminated() == false;
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.7).price(5));
        assert scenario.exits.stream().allMatch(Order::isTerminated); // exit is canceled
        assert scenario.isExitTerminated() == true;
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }

    @Test
    void imcompletedEntryTakerWillNotStopExitTakerInExclusiveExecutionMarketService() {
        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.take());
                });
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.take());
            }
        };

        TradingScenario scenario = latest();

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0.5);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }

    @Test
    void imcompletedEntryTakerWillNotStopExitTakerInNonExclusiveExecutionMarketService() {
        market.service.exclusiveExecution = false;

        new TradingScenario() {

            @Override
            protected void entry() {
                when(now(), () -> {
                    entry(Direction.BUY, 1, s -> s.take());
                });
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.take());
            }
        };

        TradingScenario scenario = latest();

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0.5);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }
}
