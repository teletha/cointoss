/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.TradeTest;

class ScenarioOrderTest extends TraderTestSupport {

    @TradeTest
    void entries(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        default:
            assert s.entries.size() == 1;
            break;
        }
    }

    @TradeTest
    void exits(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case Exit:
        case ExitCanceled:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitPartially:
        case ExitPartiallyCancelled:
        case EntryPartiallyCanceledAndExitCompletely:
            assert s.exits.size() == 1;
            break;

        case ExitCanceledThenOtherExit:
        case ExitCanceledThenOtherExitCompletely:
        case ExitOneCompletedOtherRemained:
            assert s.exits.size() == 2;
            break;

        default:
            assert s.exits.size() == 0;
            break;
        }
    }
}