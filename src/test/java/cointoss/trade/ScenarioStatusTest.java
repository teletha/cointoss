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

import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.TradeTest;

class ScenarioStatusTest extends TraderTestSupport {

    @TradeTest
    void isActive(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceled:
        case ExitCanceledThenOtherExitCompletely:
            assert s.isActive() == false;
            break;

        default:
            assert s.isActive() == true;
            break;
        }
    }

    @TradeTest
    void isCanceled(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
            assert s.isCanceled() == true;
            break;

        default:
            assert s.isCanceled() == false;
            break;
        }
    }

    @TradeTest
    void isTerminated(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceled:
        case ExitCanceledThenOtherExitCompletely:
            assert s.isTerminated() == true;
            break;

        default:
            assert s.isTerminated() == false;
            break;
        }
    }

    @TradeTest
    void isEntryTerminated(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryCompletely:
        case EntryMultiple:
        case EntrySeparately:
        case EntryPartiallyCanceled:
        case EntryPartiallyAndExitCompletely:
        case Exit:
        case ExitCanceled:
        case ExitCanceledThenOtherExit:
        case ExitCanceledThenOtherExitCompletely:
        case ExitPartially:
        case ExitPartiallyCancelled:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
            assert s.isEntryTerminated() == true;
            break;

        default:
            assert s.isEntryTerminated() == false;
            break;
        }
    }

    @TradeTest
    void isExitTerminated(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryPartiallyAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceled:
        case ExitCanceledThenOtherExitCompletely:
            assert s.isExitTerminated() == true;
            break;

        default:
            assert s.isExitTerminated() == false;
            break;
        }
    }
}
