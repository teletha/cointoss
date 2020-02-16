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

import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;

class ScenarioSizeTest extends TraderTestSupport {

    @TradeTest
    void entrySize(ScenePart scene, SizePart size) {
        Scenario s = build(scene, size);

        switch (scene) {
        case Entry:
        case EntryCancelled:
        case EntryCompletely:
        case EntryPartially:
        case EntryPartiallyCancelled:
        case Exit:
        case ExitCancelled:
        case ExitCompletely:
        case ExitPartially:
        case ExitPartiallyCancelled:
            assert s.entrySize.is(size);
        }
    }

    @TradeTest
    void entryRemainingSize(ScenePart scene, SizePart size) {
        Scenario s = build(scene, size);

        switch (scene) {
        case Entry:
        case EntryCancelled:
            assert s.entryRemainingSize().is(size);
            break;

        case EntryPartially:
        case EntryPartiallyCancelled:
            assert s.entryRemainingSize().is(size.half);
            break;

        case EntryCompletely:
        case Exit:
        case ExitCancelled:
        case ExitCompletely:
        case ExitPartially:
        case ExitPartiallyCancelled:
            assert s.entryRemainingSize().is(0);
            break;
        }
    }
}
