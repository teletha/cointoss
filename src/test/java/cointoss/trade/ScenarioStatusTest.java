/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import cointoss.order.OrderState;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.TradeTest;

class ScenarioStatusTest extends TraderTestSupport {

    @TradeTest
    void isActive(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryPartiallyCanceledAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceledThenOtherExitCompletely:
            assert s.isActive() == false;
            break;

        default:
            assert s.isActive() == true;
            break;
        }
    }

    @TradeTest
    void isTerminated(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntryCanceled:
        case EntryPartiallyCanceledAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
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
        case EntryPartiallyCanceledAndExitCompletely:
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
        case EntryPartiallyCanceledAndExitCompletely:
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

    @TradeTest
    void state(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case Entry:
        case EntryCompletely:
        case EntryMultiple:
        case EntryPartially:
        case EntryPartiallyCanceled:
        case EntrySeparately:
            assert s.state.is(OrderState.ACTIVE);
            break;

        case Exit:
        case ExitCanceled:
        case ExitCanceledThenOtherExit:
        case ExitPartially:
        case ExitPartiallyCancelled:
            assert s.state.is(OrderState.ACTIVE);
            break;

        case EntryPartiallyCanceledAndExitCompletely:
        case ExitCanceledThenOtherExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
            assert s.state.is(OrderState.COMPLETED);
            break;

        case EntryCanceled:
            assert s.state.is(OrderState.CANCELED);
        }
    }
}