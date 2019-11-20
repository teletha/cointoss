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

import org.junit.jupiter.api.Test;

import cointoss.Scenario.Snapshot;
import cointoss.execution.Execution;
import cointoss.util.Num;

class ScenarioProfitTest extends TraderTestSupport {

    @Test
    void completeEntryAndCompleteExit() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.sell(1).price(20));

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(0);
        assert s.profit(Num.of(25)).is(10);
        assert s.entryRemainingSize().is(0);
        assert s.exitRemainingSize().is(0);
    }

    @Test
    void completeEntryAndIncompleteExit() {
        entryAndExit(Execution.with.buy(2).price(10), Execution.with.sell(1).price(20));

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(15);
        assert s.profit(Num.of(25)).is(25);
        assert s.entryRemainingSize().is(0);
        assert s.exitRemainingSize().is(1);
    }

    @Test
    void completeEntryAndPartialExit() {
        entryAndExitPartial(Execution.with.buy(2).price(10), Execution.with.sell(2).price(20), 1);

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(15);
        assert s.profit(Num.of(25)).is(25);
        assert s.entryRemainingSize().is(0);
        assert s.exitRemainingSize().is(1);
    }

    @Test
    void completeEntryAndNoExit() {
        entry(Execution.with.buy(1).price(10));

        Scenario s = latest();
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(Num.of(25)).is(15);
        assert s.profit(Num.of(25)).is(15);
        assert s.entryRemainingSize().is(0);
        assert s.exitRemainingSize().is(0);
    }

    @Test
    void partialEntryAndCompleteExit() {
        entryPartialAndExit(Execution.with.buy(2).price(10), 1, Execution.with.sell(1).price(20));

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(0);
        assert s.profit(Num.of(25)).is(10);
        assert s.entryRemainingSize().is(1);
        assert s.exitRemainingSize().is(0);
    }

    @Test
    void partialEntryAndIncompleteExit() {
        entryPartialAndExit(Execution.with.buy(3).price(10), 2, Execution.with.sell(1).price(20));

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(15);
        assert s.profit(Num.of(25)).is(25);
        assert s.entryRemainingSize().is(1);
        assert s.exitRemainingSize().is(1);
    }

    @Test
    void partialEntryAndPartialExit() {
        entryPartialAndExitPartial(Execution.with.buy(2).price(10), 1, Execution.with.sell(2).price(20), 1);

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(0);
        assert s.profit(Num.of(25)).is(10);
        assert s.entryRemainingSize().is(1);
        assert s.exitRemainingSize().is(0);
    }

    @Test
    void partialEntryAndNoExit() {
        entryPartial(Execution.with.buy(2).price(10), 1);

        Scenario s = latest();
        assert s.realizedProfit.is(0);
        assert s.unrealizedProfit(Num.of(25)).is(15);
        assert s.profit(Num.of(25)).is(15);
        assert s.entryRemainingSize().is(1);
        assert s.exitRemainingSize().is(0);
    }

    @Test
    void snapshotCompleteEntryAndCompleteExit() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.sell(1).price(20).date(base.plus(5, MINUTES)));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(0);
    }

    @Test
    void snapshotCompleteEntryAndIncompleteExit() {
        entryAndExit(Execution.with.buy(2).price(10), Execution.with.sell(1).price(20).date(base.plus(5, MINUTES)));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(4);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-8);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(10);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
    }

    @Test
    void snapshotCompleteEntryAndPartialExit() {
        entryAndExitPartial(Execution.with.buy(2).price(10), Execution.with.sell(2).price(20).date(base.plus(5, MINUTES)), 1);

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(4);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-8);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(10);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
    }

    @Test
    void snapshotCompleteEntryAndNoExit() {
        entry(Execution.with.buy(1).price(10));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
    }

    @Test
    void snapshotPartialEntryAndCompleteExit() {
        entryPartialAndExit(Execution.with.buy(2).price(10), 1, Execution.with.sell(1).price(20).date(base.plus(5, MINUTES)));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(0);
    }

    @Test
    void snapshotPartialEntryAndIncompleteExit() {
        entryPartialAndExit(Execution.with.buy(3).price(10), 2, Execution.with.sell(1).price(20).date(base.plus(5, MINUTES)));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(4);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-8);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(10);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
    }

    @Test
    void snapshotPartialEntryAndPartialExit() {
        entryPartialAndExitPartial(Execution.with.buy(2).price(10), 1, Execution.with.sell(2).price(20).date(base.plus(5, MINUTES)), 1);

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(0);
    }

    @Test
    void snapshotPartialEntryAndNoExit() {
        entryPartial(Execution.with.buy(2).price(10), 1);

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(1, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = s.snapshotAt(base.plus(2, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = s.snapshotAt(base.plus(4, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = s.snapshotAt(base.plus(6, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = s.snapshotAt(base.plus(10, MINUTES));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
    }

    @Test
    void snapshotDontCareSeconds() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1.5, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        market.perform(Execution.with.buy(0.5).price(9).date(base.plus(20, SECONDS)));
        market.perform(Execution.with.buy(0.5).price(9).date(base.plus(40, SECONDS)));
        market.perform(Execution.with.buy(0.5).price(9).date(base.plus(60, SECONDS)));

        Scenario s = latest();
        // past
        Snapshot snapshot = s.snapshotAt(base.plus(30, SECONDS));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(0);

        // past
        snapshot = s.snapshotAt(base.plus(59, SECONDS));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(0);

        // now
        snapshot = s.snapshotAt(base.plus(60, SECONDS));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(3);

        // future
        snapshot = s.snapshotAt(base.plus(80, SECONDS));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(3);
    }
}
