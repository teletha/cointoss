/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Direction;
import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;

class ScenarioSizeTest extends TraderTestSupport {

    @TradeTest
    void entrySize(SizePart size) {
        Scenario s = entry(Direction.BUY, size, o -> o.make(10));
        assert s.entrySize.is(size);

        executeEntryHalf();
        assert s.entrySize.is(size);

        executeEntryAll();
        assert s.entrySize.is(size);

        exit(o -> o.make(20));
        assert s.entrySize.is(size);

        executeExitHalf();
        assert s.entrySize.is(size);

        executeExitAll();
        assert s.entrySize.is(size);

        s = entry(Direction.BUY, size, o -> o.make(10));
        cancelEntry();
        assert s.entrySize.is(size);

        s = entry(Direction.BUY, size, o -> o.make(10));
        executeEntryHalf();
        cancelEntry();
        assert s.entrySize.is(size);
    }

    @TradeTest
    void entryRemainingSize(SizePart size) {
        Scenario s = entry(Direction.BUY, size, o -> o.make(10));
        assert s.entryRemainingSize().is(size);

        executeEntryHalf();
        assert s.entryRemainingSize().is(size.half);

        executeEntryAll();
        assert s.entryRemainingSize().is(0);

        exit(o -> o.make(20));
        assert s.entryRemainingSize().is(0);

        executeExitHalf();
        assert s.entryRemainingSize().is(0);

        executeExitAll();
        assert s.entryRemainingSize().is(0);

        s = entry(Direction.BUY, size, o -> o.make(10));
        cancelEntry();
        assert s.entryRemainingSize().is(0);

        s = entry(Direction.BUY, size, o -> o.make(10));
        executeEntryHalf();
        cancelEntry();
        assert s.entryRemainingSize().is(0);
    }
}
