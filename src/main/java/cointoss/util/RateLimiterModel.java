/*
 * Copyright (C) 2024 The COINTOSS Development Team
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

import cointoss.util.RateLimit.Rate;
import icy.manipulator.Icy;
import kiss.I;

@Icy
abstract class RateLimiterModel {

    /** The threshold of the overload mode. */
    private long thresholdOverload;

    /** The minimum time to refill 1 weight. */
    private long refillTime;

    /** The restorable info holder. */
    private Rate info = new Rate();

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
     * Configure the capacity refresh time.
     * 
     * @return
     */
    @Icy.Overload("refresh")
    private Duration refreshSecond(int time) {
        return refresh(time, TimeUnit.SECONDS);
    }

    /**
     * Configure the capacity refresh time.
     * 
     * @return
     */
    @Icy.Overload("refresh")
    private Duration refreshMinute(int time) {
        return refresh(time, TimeUnit.MINUTES);
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
        refillTime = refresh.toMillis() / limit();
        return refresh;
    }

    /**
     * Identifiable key.
     * 
     * @return
     */
    @Icy.Property
    public Object persistable() {
        return null;
    }

    @Icy.Intercept("persistable")
    private Object persist(Object name) {
        if (name != null) {
            info = I.make(RateLimit.class).rate.computeIfAbsent(name.toString(), _ -> new Rate());

            I.info("Initialize RateLimit [name:%s  permits:%d  time:%s"
                    .formatted(name, info.usingPermits, Chrono.utcByMills(info.lastAccessedTime)));
        }
        return name;
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

        long now = System.currentTimeMillis();
        long elapsedTime = now - info.lastAccessedTime;
        long refilledPermits = elapsedTime / refillTime;
        info.usingPermits -= refilledPermits;
        if (info.usingPermits < 0) {
            info.usingPermits = 0;
        }

        long nextUsingPermits = info.usingPermits + weight;
        long overloadedPermits = nextUsingPermits - thresholdOverload;

        if (overloadedPermits <= 1 /* Not 0 */) {
            // allow to access
            info.usingPermits = nextUsingPermits;
            info.lastAccessedTime = now;
            if (persistable() != null) RateLimit.SAVE.accept(this);
            return; // immediately
        } else {
            // wait to access
            try {
                TimeUnit.MILLISECONDS.sleep(overloadedPermits * refillTime);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
            acquire(weight);
        }
    }
}