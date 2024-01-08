/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

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
     * Modify time by lag.
     * 
     * @return The modified time.
     */
    long lag();

    /**
     * Build no lag.
     * 
     * @return A create {@link Latency}.
     */
    static Latency zero() {
        return new Latency() {

            @Override
            public long lag() {
                return 0;
            }

            @Override
            public ZonedDateTime emulate(ZonedDateTime time) {
                return time;
            }
        };
    }

    /**
     * Build fixed time lag.
     * 
     * @param time A time.
     * @param unit A time unit.
     * @return A created {@link Latency}.
     */
    static Latency fixed(long time, ChronoUnit unit) {
        long lag = TimeUnit.of(unit).toMillis(time);

        return new Latency() {

            @Override
            public long lag() {
                return lag;
            }

            @Override
            public ZonedDateTime emulate(ZonedDateTime date) {
                return date.plus(time, unit);
            }
        };
    }
}