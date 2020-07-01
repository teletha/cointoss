/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade.extension;

import java.time.Duration;

import cointoss.util.Chrono;

public class HoldTimePart implements TradePart {

    public final Duration sec;

    public HoldTimePart(int seconds) {
        this.sec = Duration.ofSeconds(seconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Chrono.formatAsDuration(sec);
    }
}