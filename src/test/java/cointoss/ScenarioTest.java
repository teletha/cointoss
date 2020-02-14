/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static java.time.temporal.ChronoUnit.*;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.order.Order;

class ScenarioTest extends TraderTestSupport {

    @Test
    void holdTime() {
        entryAndExit(Execution.with.buy(1).price(10).date(afterSecond(0)), Execution.with.buy(1).price(20).date(afterSecond(10)));

        Scenario s = latest();
        assert s.holdTime().equals(Duration.ofSeconds(10));
    }

    @Test
    void holdTimeWithNoneExecutedActiveEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        market.elapse(14, SECONDS);

        Scenario s = latest();
        assert s.holdTime().equals(Duration.ofSeconds(14));
    }

    @Test
    void holdTimeWithNoneExecutedCanceledEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10).cancelAfter(8, SECONDS));
            }

            @Override
            protected void exit() {
            }
        });

        market.elapse(18, SECONDS);

        Scenario s = latest();
        assert s.holdTime().isZero();
    }

    @Test
    void holdTimeWithPartialyExecutedCanceledEntry() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10).cancelAfter(8, SECONDS));
            }

            @Override
            protected void exit() {
            }
        });

        market.perform(Execution.with.buy(0.1).price(9));
        market.elapse(18, SECONDS);

        Scenario s = latest();
        assert s.holdTime().equals(Duration.ofSeconds(18));
    }

    @Test
    void isTerminated() {
        entry(Execution.with.buy(1).price(10));

        Scenario s = latest();
        assert s.isTerminated() == false;
    }

    @Test
    void isEntryTerminated() {
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
        assert s.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert s.isEntryTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(9));
        assert s.isEntryTerminated() == true;
    }

    @Test
    void isExitTerminated() {
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
        assert s.isExitTerminated() == false;

        market.perform(Execution.with.buy(1).price(9));
        assert s.isExitTerminated() == false;

        awaitOrderBufferingTime();

        market.perform(Execution.with.buy(0.5).price(21));
        assert s.isExitTerminated() == false;

        market.perform(Execution.with.buy(0.5).price(21));
        assert s.isExitTerminated() == true;
    }

    @Test
    void entrySize() {
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
        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0);
        assert s.entryRemainingSize2.is(1);

        // first execution
        market.perform(Execution.with.buy(0.3).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.3);
        assert s.entryRemainingSize2.is(0.7);

        // second execution
        market.perform(Execution.with.buy(0.3).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.6);

        // third execution
        market.perform(Execution.with.buy(0.5).price(9));
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
    }

    @Test
    void entryWithMultipleExecutionsAndSingleExit() {
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
        assert s.exits.size() == 0;

        market.perform(Execution.with.buy(0.1).price(9));
        market.perform(Execution.with.buy(0.2).price(9));
        market.perform(Execution.with.buy(0.3).price(9));
        market.perform(Execution.with.buy(0.4).price(9));

        awaitOrderBufferingTime();
        assert s.exits.size() == 1;
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
    }

    @Test
    void entryWithMultipleExecutionsAndMultipleExits() {
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
        assert s.exits.size() == 0;

        // first entry
        market.perform(Execution.with.buy(0.1).price(9));
        market.perform(Execution.with.buy(0.2).price(9));

        awaitOrderBufferingTime();
        assert s.exits.size() == 1;
        assert s.exitSize.is(0.3);
        assert s.exitExecutedSize.is(0);

        // second entry
        market.perform(Execution.with.buy(0.3).price(9));

        awaitOrderBufferingTime();
        assert s.exits.size() == 2;
        assert s.exitSize.is(0.6);
        assert s.exitExecutedSize.is(0);

        // third entry
        market.perform(Execution.with.buy(0.5).price(9));

        awaitOrderBufferingTime();
        assert s.exits.size() == 3;
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);
    }

    @Test
    void cancelEntryByTime() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10).cancelAfter(5, SECONDS));
            }

            @Override
            protected void exit() {
            }
        });

        Scenario s = latest();

        market.perform(Execution.with.buy(1).price(15));
        assert s.entrySize.is(1);
        assert s.isEntryTerminated() == false;

        market.elapse(5, SECONDS);
        market.perform(Execution.with.buy(1).price(15));
        assert s.entrySize.is(1);
        assert s.isEntryTerminated() == true;
    }

    @Test
    void executingExitWillCancelRemainingEntry() {
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
        assert s.exits.size() == 0;

        // divided entries
        market.perform(Execution.with.buy(0.2).price(9));
        awaitOrderBufferingTime();
        market.perform(Execution.with.buy(0.3).price(9));
        awaitOrderBufferingTime();
        market.perform(Execution.with.buy(0.4).price(9));
        awaitOrderBufferingTime();

        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize().is(0.9);
        assert s.isEntryTerminated() == false;
        assert s.entries.get(0).isCanceled() == false;
        assert s.exits.size() == 3;
        assert s.exitSize.is(0.9);
        assert s.exitExecutedSize.is(0);
        assert s.isExitTerminated() == false;

        // divided exits
        market.perform(Execution.with.sell(0.5).price(21));
        assert s.exitExecutedSize.is(0.5);
        assert s.isEntryTerminated() == true;
        assert s.entries.size() == 1;
        assert s.entries.get(0).isCanceled() == true;
        market.perform(Execution.with.sell(0.4).price(21));
        assert s.exitExecutedSize.is(0.9);
    }

    @Test
    void exitAndStop() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
                exitAt(5);
            }
        });

        Scenario s = latest();
        assert s.exits.size() == 0;

        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert s.exits.size() == 1; // exit is ordered
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.1).price(5)); // trigger stop
        market.perform(Execution.with.buy(0.5).price(5));
        assert s.exits.size() == 2; // stop is ordered
        assert s.exits.stream().allMatch(Order::isActive);
        assert s.isExitTerminated() == false;
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.7).price(5));
        assert s.exits.stream().allMatch(Order::isTerminated); // exit is canceled
        assert s.isExitTerminated() == true;
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(1);
    }

    @Test
    void stopLossTaker() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(1700));
            }

            @Override
            protected void exit() {
                exitAt(entryPrice.plus(this, 1000));
                exitAt(entryPrice.minus(this, 500), s -> s.take());
            }
        });

        Scenario s = latest();

        market.perform(Execution.with.buy(1.316).price(1850));
        market.perform(Execution.with.buy(0.1).price(1036));
        market.perform(Execution.with.buy(0.25).price(1247));
        market.perform(Execution.with.buy(2.2).price(1850));
        market.perform(Execution.with.buy(4.6823314).price(1146));
        market.perform(Execution.with.buy(1.4).price(1146));
        System.out.println(s);
        assert s.exits.size() == 1;
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(1);
    }

    @Test
    @Disabled
    void imcompletedEntryTakerWillNotStopExitTakerInExclusiveExecutionMarketService() {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.take());
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.take());
            }
        });

        Scenario s = latest();

        market.perform(Execution.with.buy(0.5).price(15));
        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(0.5);
        assert s.exits.size() == 1;
        assert s.exitSize.is(0.5);
        assert s.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(15));
        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.exits.size() == 2;
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0);

        market.perform(Execution.with.buy(0.5).price(15));
        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.exits.size() == 2;
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(0.5);

        market.perform(Execution.with.buy(0.5).price(15));
        assert s.entries.size() == 1;
        assert s.entrySize.is(1);
        assert s.entryExecutedSize.is(1);
        assert s.exits.size() == 2;
        assert s.exitSize.is(1);
        assert s.exitExecutedSize.is(1);
    }

    @Test
    void testName() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1, s -> s.make(10));
            }

            @Override
            protected void exit() {
                exitAt(20);
                exitWhen(market.timeline.take(e -> e.price.isGreaterThan(20)), o -> o.take());
            }
        });

        Scenario s = latest();
        assert s.exits.size() == 0;

        market.perform(Execution.with.buy(1).price(9));
        market.elapse(1, SECONDS);
        assert s.exits.size() == 2; // exit is ordered
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(0);
        assert s.exitSize.is(2);

        market.perform(Execution.with.buy(2).price(21)); // trigger stop
        assert s.exits.size() == 2; // stop is ordered
        assert s.entryExecutedSize.is(1);
        assert s.exitExecutedSize.is(1);
        assert s.exitSize.is(1);
    }
}
