/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Emulation for network latency.
 */
public interface Latency {

    /**
     * Modify time by lag.
     * 
     * @return The modified time.
     */
    ZonedDateTime emulate(ZonedDateTime time);

    /**
     * Build no lag.
     * 
     * @return A create {@link Latency}.
     */
    static Latency zero() {
        return date -> date;
    }

    /**
     * Build fixed time lag.
     * 
     * @param time A time.
     * @param unit A time unit.
     * @return A created {@link Latency}.
     */
    static Latency fixed(long time, ChronoUnit unit) {
        return date -> date.plus(time, unit);
    }
}
