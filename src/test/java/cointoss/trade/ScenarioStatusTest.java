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

import cointoss.Scenario;
import cointoss.TraderTestSupport;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.TradeTest;

class ScenarioStatusTest extends TraderTestSupport {

    @TradeTest
    void isActive(ScenePart scene) {
        Scenario s = build(scene);

        switch (scene) {
        case EntryCancelled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
            assert s.isActive() == false;
            break;

        default:
            assert s.isActive() == true;
            break;
        }
    }

    @TradeTest
    void isCanceled(ScenePart scene) {
        Scenario s = build(scene);

        switch (scene) {
        case EntryCancelled:
            assert s.isCanceled() == true;
            break;

        default:
            assert s.isCanceled() == false;
            break;
        }
    }

    @TradeTest
    void isTerminated(ScenePart scene) {
        Scenario s = build(scene);

        switch (scene) {
        case EntryCancelled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
            assert s.isTerminated() == true;
            break;

        default:
            assert s.isTerminated() == false;
            break;
        }
    }

    @TradeTest
    void isEntryTerminated(ScenePart scene) {
        Scenario s = build(scene);

        switch (scene) {
        case EntryCancelled:
        case EntryCompletely:
        case EntryPartiallyAndExitCompletely:
        case Exit:
        case ExitCancelled:
        case ExitPartially:
        case ExitPartiallyCancelled:
        case ExitCompletely:
            assert s.isEntryTerminated() == true;
            break;

        default:
            assert s.isEntryTerminated() == false;
            break;
        }
    }

    @TradeTest
    void isExitTerminated(ScenePart scene) {
        Scenario s = build(scene);

        switch (scene) {
        case EntryCancelled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
            assert s.isExitTerminated() == true;
            break;

        default:
            assert s.isExitTerminated() == false;
            break;
        }
    }
}
