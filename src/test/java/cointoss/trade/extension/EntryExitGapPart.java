/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade.extension;

import java.time.Duration;

import cointoss.util.Chrono;

public class EntryExitGapPart implements TradePart {

    public final Duration sec;

    public EntryExitGapPart(int seconds) {
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
