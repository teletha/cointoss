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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.TradeTest;

class ScenaroHoldTimeTest extends TraderTestSupport {

    @TradeTest
    void holdTime(ScenePart scene) {
        Scenario s = scenario(scene);

        switch (scene) {
        case EntrySeparately:
        case ExitSeparately:
            assert s.holdTime().equals(Duration.ofSeconds(30));
            break;

        default:
            assert s.holdTime().equals(Duration.ofSeconds(0));
            break;
        }

        // Time flows...
        market.elapse(100, ChronoUnit.SECONDS);

        switch (scene) {
        // already completed so no change
        case EntryCanceled:
        case EntryPartiallyCanceledAndExitCompletely:
        case ExitCanceled:
        case ExitCompletely:
        case ExitMultiple:
        case ExitCanceledThenOtherExitCompletely:
            assert s.holdTime().equals(Duration.ofSeconds(0));
            break;

        // already completed so no change
        case ExitSeparately:
            assert s.holdTime().equals(Duration.ofSeconds(30));
            break;

        // added the elapsed time
        case EntrySeparately:
            assert s.holdTime().equals(Duration.ofSeconds(130));
            break;

        // added the elapsed time
        default:
            assert s.holdTime().equals(Duration.ofSeconds(100));
            break;
        }
    }

    @TradeTest
    void holdStartTime(ScenePart scene) {
        ZonedDateTime starting = market.service.now();
        Scenario s = scenario(scene);
        assert s.holdStartTime().isEqual(starting);

        // Time flows...
        market.elapse(100, ChronoUnit.SECONDS);
        assert s.holdStartTime().isEqual(starting);
    }

    @TradeTest
    void holdEndTime(ScenePart scene) {
        ZonedDateTime starting = market.service.now();
        Scenario s = scenario(scene);

        switch (scene) {
        case EntrySeparately:
        case ExitSeparately:
            assert s.holdEndTime().isEqual(starting.plusSeconds(30));
            break;

        default:
            assert s.holdEndTime().isEqual(starting);
            break;
        }
    }
}
