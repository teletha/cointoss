/*
 * Copyright (C) 2019 CoinToss Development Team
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
import java.util.Objects;

import org.apache.commons.lang3.RandomUtils;

import cointoss.util.Chrono;

/**
 * Network time lag emulator.
 */
public class TimeLag {

    /** The base time */
    public static final ZonedDateTime Base = Chrono.utcNow().truncatedTo(ChronoUnit.DAYS);

    /** The minimum time lag (ms). */
    private final int min;

    /** The maximum time lag (ms). */
    private final int max;

    /**
     * Create {@link TimeLag} setting.
     * 
     * @param min The fixed time lag (ms).
     */
    public TimeLag(int fixed) {
        this(fixed, fixed);
    }

    /**
     * Create {@link TimeLag} setting.
     * 
     * @param min The minimum time lag (ms).
     * @param max The maximum time lag (ms).
     */
    public TimeLag(int min, int max) {
        Objects.checkFromToIndex(min, max, max);

        this.min = min;
        this.max = max;
    }

    /**
     * Convert to {@link ZonedDateTime}.
     * 
     * @return
     */
    public ZonedDateTime to() {
        return Base.plusSeconds(RandomUtils.nextInt(min, max));
    }

    /**
     * @return
     */
    public long generate() {
        return RandomUtils.nextLong(min * 1000000000L, max * 1000000000L);
    }
}
