/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

public enum Trend {
    Buy, Sell, Range, Unknown;

    private static boolean rangeDirection(Tick tick) {
        int count = 2;
        if (tick.isBear()) {
            if (!tick.ticker.ticks.before(tick, count).stream().allMatch(Tick::isBear)) {
                return true;
            }
        } else {
            if (!tick.ticker.ticks.before(tick, count).stream().allMatch(Tick::isBull)) {
                return true;
            }
        }
        return false;
    }
}
