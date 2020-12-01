/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

public class RateLimit {

    /**
     * Limit rate per your time.
     * 
     * @param capacity
     * @param time
     * @param unit
     * @return
     */
    public static Bucket per(int capacity, int time, TimeUnit unit) {
        return Bucket4j.builder().addLimit(Bandwidth.simple(capacity, Duration.of(time, unit.toChronoUnit()))).build();
    }
}
