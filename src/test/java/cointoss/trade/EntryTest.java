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
        VerifiableScenario verifiable = entryAndExit(Execution.with.buy(1).price(10).date(second(0)), Execution.with.buy(1)
                .price(20)
                .date(second(10)));

        Scenario scenario = verifiable.verify();
        assert scenario.holdTime().equals(Duration.ofSeconds(10));
    }

    @Test
    void isTerminated() {
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
        verifiable.entry(Execution.with.buy(1).price(11));
        assert scenario.isTerminated() == false;
    }

    @Test
    void isEntryTerminated() {
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
        assert scenario.isEntryTerminated() == false;

        verifiable.market.perform(Execution.with.buy(0.5).price(9));
        assert scenario.isEntryTerminated() == false;

        verifiable.market.perform(Execution.with.buy(0.5).price(9));
        assert scenario.isEntryTerminated() == true;
    }

    @Test
    void isExitTerminated() {
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
        assert scenario.isExitTerminated() == false;

        verifiable.market.perform(Execution.with.buy(1).price(9));
        assert scenario.isExitTerminated() == false;

        verifiable.market.elapse(1, SECONDS);
        verifiable.market.perform(Execution.with.buy(0.5).price(21));
        assert scenario.isExitTerminated() == false;

        verifiable.market.perform(Execution.with.buy(0.5).price(21));
        assert scenario.isExitTerminated() == true;
    }

    @Test
    void entryWithMultipleExecutionAndExitAtPrice() {
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
        assert scenario.exits.size() == 0;

        verifiable.market.perform(Execution.with.buy(0.1).price(9));
        verifiable.market.perform(Execution.with.buy(0.2).price(9));
        verifiable.market.perform(Execution.with.buy(0.3).price(9));
        verifiable.market.perform(Execution.with.buy(0.4).price(9));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.exits.size() == 1;
    }

    @Test
    void exitAndStop() {
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
                exitAt(5);
            }
        };

        Scenario scenario = verifiable.verify();
        assert scenario.exits.size() == 0;

        verifiable.market.perform(Execution.with.buy(1).price(9));
        verifiable.market.elapse(1, SECONDS);
        assert scenario.exits.size() == 1; // exit is ordered
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(0);

        verifiable.market.perform(Execution.with.buy(0.1).price(5)); // trigger stop
        verifiable.market.perform(Execution.with.buy(0.5).price(5));
        assert scenario.exits.size() == 2; // stop is ordered
        assert scenario.exits.stream().allMatch(Order::isActive);
        assert scenario.isExitTerminated() == false;
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        verifiable.market.perform(Execution.with.buy(0.7).price(5));
        assert scenario.exits.stream().allMatch(Order::isTerminated); // exit is canceled
        assert scenario.isExitTerminated() == true;
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }

    @Test
    void imcompletedEntryTakerWillNotStopExitTakerInExclusiveExecutionMarketService() {
        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 1, s -> s.take());
                });
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.take());
            }
        };

        Scenario scenario = verifiable.verify();

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0.5);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }

    @Test
    void imcompletedEntryTakerWillNotStopExitTakerInNonExclusiveExecutionMarketService() {

        VerifiableScenario verifiable = new VerifiableScenario() {

            @Override
            protected void entry() {
                when(now(), v -> {
                    entry(Direction.BUY, 1, s -> s.take());
                });
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.take());
            }
        };
        verifiable.market.service.exclusiveExecution = false;

        Scenario scenario = verifiable.verify();

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(0.5);
        assert scenario.exitSize.is(0.5);
        assert scenario.exitExecutedSize.is(0);

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(0.5);

        verifiable.market.perform(Execution.with.buy(0.5).price(15));
        assert scenario.entrySize.is(1);
        assert scenario.entryExecutedSize.is(1);
        assert scenario.exitSize.is(1);
        assert scenario.exitExecutedSize.is(1);
    }
}
