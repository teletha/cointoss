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
        default:
            assert s.entrySize.is(size);
        }
    }

    @TradeTest
    void entryExecutedSize(ScenePart scene, SizePart size) {
        Scenario s = build(scene, size);

        switch (scene) {
        case Entry:
        case EntryCancelled:
            assert s.entryExecutedSize().is(0);
            break;

        case EntryPartially:
        case EntryPartiallyCancelled:
        case EntryPartiallyAndExitCompletely:
            assert s.entryExecutedSize().is(size.half);
            break;

        default:
            assert s.entryExecutedSize().is(size);
            break;
        }
    }
}
