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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketListener;
import kiss.Signaling;
import kiss.Storable;

public class RateLimit implements Storable<RateLimit> {

    /** The singleton instance. */
    private static final RateLimit instance = new RateLimit();

    public final Map<String, Remaining> remainings = new HashMap();

    /** Do not perform the saving process each time, but slightly reduce it. */
    private final Signaling requestSaving = new Signaling();

    /**
     * Hide constructor.
     */
    private RateLimit() {
        restore();
        requestSaving.expose.throttle(15, TimeUnit.SECONDS).to(this::store);
    }

    /**
     * Limit rate per your time.
     * 
     * @param capacity
     * @param time
     * @param unit
     * @return
     */
    public static Bucket per(int capacity, int time, TimeUnit unit) {
        Bucket bucket = Bucket4j.builder().addLimit(Bandwidth.simple(capacity, Duration.of(time, unit.toChronoUnit()))).build();

        return bucket;
    }

    /**
     * Limit rate per your time.
     * 
     * @param name The limiter's name.
     * @param capacity
     * @param time
     * @param unit
     * @return
     */
    public static Bucket per(String name, int capacity, int time, TimeUnit unit) {
        Remaining remaining = instance.remainings.getOrDefault(name, new Remaining(0, Long.MAX_VALUE));
        long elapsedMills = System.currentTimeMillis() - remaining.time;
        long initial = elapsedMills <= unit.toMillis(time) ? capacity : remaining.amount;

        Bucket bucket = Bucket4j.builder()
                .addLimit(Bandwidth.simple(capacity, Duration.of(time, unit.toChronoUnit())).withInitialTokens(initial))
                .build();

        return bucket.toListenable(new BucketListener() {

            @Override
            public void onRejected(long tokens) {
            }

            @Override
            public void onParked(long nanos) {
            }

            @Override
            public void onInterrupted(InterruptedException e) {
            }

            @Override
            public void onDelayed(long nanos) {
            }

            @Override
            public void onConsumed(long tokens) {
                instance.remainings.put(name, new Remaining(System.currentTimeMillis(), bucket.getAvailableTokens()));
                instance.requestSaving.accept("");
            }
        });
    }

    /**
     * 
     */
    private record Remaining(long time, long amount) {
    }
}
