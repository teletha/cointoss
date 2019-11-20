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

import org.junit.jupiter.api.Test;

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
        entryPartialAndExit(Execution.with.buy(2).price(10), 1, Execution.with.sell(1).price(20));

        Scenario s = latest();
        assert s.realizedProfit.is(10);
        assert s.unrealizedProfit(Num.of(25)).is(0);
        assert s.profit(Num.of(25)).is(10);
        assert s.entryRemainingSize().is(1);
        assert s.exitRemainingSize().is(0);
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
}
