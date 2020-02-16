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

import static java.time.temporal.ChronoUnit.MINUTES;

import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.TradeTest;

class ScenarioStatusTest extends TraderTestSupport {

    @TradeTest
    void isActive(SidePart side) {
        // canceled
        Scenario s = entry(side, 5, o -> o.make(10).cancelAfter(10, MINUTES));
        market.elapse(10, MINUTES);
        assert s.isActive() == false;

        // create entry
        s = entry(side, 5, o -> o.make(10));
        assert s.isActive() == true;

        // executed entry partially
        executeEntryHalf();
        assert s.isActive() == true;

        // complete entry
        executeEntryAll();
        assert s.isActive() == true;

        // create exit
        exit(o -> o.make(20));
        assert s.isActive();

        // executed exit partially
        executeExit(2, 20);
        assert s.isActive();

        // complete exit
        executeExit(3, 20);
        assert s.isActive() == false;
    }

    @TradeTest
    void isCanceled(SidePart side) {
        // canceled
        Scenario s = entry(side, 5, o -> o.make(10).cancelAfter(10, MINUTES));
        market.elapse(10, MINUTES);
        assert s.isCanceled() == true;

        // create entry
        s = entry(side, 5, o -> o.make(10));
        assert s.isCanceled() == false;

        // executed entry partially
        executeEntry(2, 10);
        assert s.isCanceled() == false;

        // complete entry
        executeEntry(3, 10);
        assert s.isCanceled() == false;

        // create exit
        exit(o -> o.make(20));
        assert s.isCanceled() == false;

        // executed exit partially
        executeExit(2, 20);
        assert s.isCanceled() == false;

        // complete exit
        executeExit(3, 20);
        assert s.isCanceled() == false;
    }

    @TradeTest
    void isCompleted(SidePart side) {
        // canceled
        Scenario s = entry(side, 5, o -> o.make(10).cancelAfter(10, MINUTES));
        market.elapse(10, MINUTES);
        assert s.isTerminated() == true;

        // create entry
        s = entry(side, 1, o -> o.make(10));
        assert s.isTerminated() == false;

        // executed entry partially
        executeEntry(1, 10);
        assert s.isTerminated() == false;

        // complete entry
        executeEntry(4, 10);
        assert s.isTerminated() == false;
    }
}
