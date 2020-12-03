/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import icy.manipulator.Icy;
import kiss.I;

@Icy
abstract class APILimiterModel {

    /** The current using permits. */
    private long usingPermits = 0;

    /** The threshold of the overload mode. */
    private long thresholdOverload;

    /** The minimum time to refill 1 weight. */
    private long refillTime;

    /** The latest access time. */
    private long lastAccessedTime;

    /**
     * Configure the access capacity.
     * 
     * @return
     */
    @Icy.Property
    public abstract int limit();

    /**
     * Configure the capacity refresh time.
     * 
     * @return
     */
    @Icy.Property
    public abstract Duration refresh();

    /**
     * Configure the capacity refresh time.
     * 
     * @return
     */
    @Icy.Overload("refresh")
    private Duration refresh(int time, TimeUnit unit) {
        return Duration.of(time, unit.toChronoUnit());
    }

    /**
     * Configure internally.
     * 
     * @param refresh
     * @return
     */
    @Icy.Intercept("refresh")
    private Duration config(Duration refresh) {
        thresholdOverload = limit() / 2;
        refillTime = refresh.toNanos() * 2 / limit();
        return refresh;
    }

    /**
     * Get access rights. If not, wait until the rights can be acquired.
     */
    public final void acquire() {
        acquire(1);
    }

    /**
     * Get access rights. If not, wait until the rights can be acquired.
     * 
     * @param weight The weight to access.
     */
    public final void acquire(int weight) {
        if (weight < 1) {
            weight = 1;
        }

        long now = System.nanoTime();
        long elapsedTime = now - lastAccessedTime;
        long refilledPermits = elapsedTime / refillTime;
        usingPermits -= refilledPermits;
        if (usingPermits < 0) {
            usingPermits = 0;
        }

        long nextUsingPermits = usingPermits + weight;
        long overloadedPermits = nextUsingPermits - thresholdOverload;

        if (overloadedPermits <= 1 /* Not 0 */) {
            // allow to access
            usingPermits = nextUsingPermits;
            lastAccessedTime = now;
            return; // immediately
        } else {
            // wait to access
            try {
                TimeUnit.NANOSECONDS.sleep(overloadedPermits * refillTime);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
            acquire(weight);
        }
    }
}