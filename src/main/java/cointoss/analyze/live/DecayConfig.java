/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze.live;

import java.io.Serializable;
import java.time.Duration;

public final class DecayConfig implements Serializable {
    public static final DecayConfig NEVER = new DecayConfig();

    public final double multiplier;

    public final long period;

    /**
     * DecayConfig constructor with no period. This is best used when you want to decay values
     * exponentially over time and the values will never be "invalid". A typical value for
     * "multiplier" is 0.95.
     *
     * @param multiplier The exponential moving average coefficient
     */
    public DecayConfig(final double multiplier) {
        if (multiplier < 0 || multiplier >= 1) {
            throw new IllegalArgumentException("Multiplier must be >= 0 and < 1");
        }
        this.multiplier = multiplier;
        this.period = 0;
    }

    /**
     * DecayConfig constructor with a period. This is best used when the data has a strict time
     * bound, where it would not make sense to keep values around forever. A typical value for
     * "multiplier" is 0.95 and the period depends on how long the data is valid.
     *
     * @param multiplier The exponential moving average coefficient
     * @param period The time period where the data is valid
     */
    public DecayConfig(final double multiplier, final Duration period) {
        if (multiplier < 0 || multiplier >= 1) {
            throw new IllegalArgumentException("Multiplier must be >= 0 and < 1");
        }
        if (period == null || period.isNegative() || period.isZero()) {
            throw new NullPointerException("Period must be specified and positive");
        }
        this.multiplier = multiplier;
        this.period = period.toNanos();
    }

    private DecayConfig() {
        multiplier = 1;
        period = 0;
    }
}
