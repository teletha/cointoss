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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icy.manipulator.Icy;
import kiss.I;
import kiss.Managed;
import kiss.Signaling;
import kiss.Storable;

@Icy
abstract class RateLimiterModel {

    /** The current using permits. */
    @Managed
    private long usingPermits = 0;

    /** The threshold of the overload mode. */
    private long thresholdOverload;

    /** The minimum time to refill 1 weight. */
    private long refillTime;

    /** The latest access time. */
    @Managed
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
        refillTime = refresh.toNanos() / limit();
        return refresh;
    }

    /**
     * Identifiable name.
     * 
     * @return
     */
    @Icy.Property
    public String persistable() {
        return null;
    }

    @Icy.Intercept("persistable")
    private String register(String name) {
        if (name != null) {
            IndirectReference indirect = database.ref.computeIfAbsent(name, k -> new IndirectReference());
            usingPermits = indirect.usingPermits;
            lastAccessedTime = indirect.lastAccessedTime;
            indirect.ref = this;
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
            if (persistable() != null) save.accept(this);
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

    /** The singleton instance. */
    private static final Persist database = new Persist();

    /** The save request. */
    private static final Signaling save = new Signaling();
    static {
        save.expose.throttle(10, SECONDS).to(database::store);
    }

    /**
     * 
     */
    private static class Persist implements Storable<Persist> {

        public Map<String, IndirectReference> ref = new HashMap();

        /**
         * Hide constructor.
         */
        private Persist() {
            restore();
        }
    }

    /**
     * To avoid complex NPE at restoration phase.
     */
    static class IndirectReference {

        private RateLimiterModel ref;

        private long usingPermits;

        private long lastAccessedTime;

        /**
         * Get the usingPermits property of this {@link RateLimiterModel.IndirectReference}.
         * 
         * @return The usingPermits property.
         */
        long getUsingPermits() {
            return ref.usingPermits;
        }

        /**
         * Set the usingPermits property of this {@link RateLimiterModel.IndirectReference}.
         * 
         * @param usingPermits The usingPermits value to set.
         */
        void setUsingPermits(long usingPermits) {
            this.usingPermits = usingPermits;
        }

        /**
         * Get the lastAccessedTime property of this {@link RateLimiterModel.IndirectReference}.
         * 
         * @return The lastAccessedTime property.
         */
        long getLastAccessedTime() {
            return ref.lastAccessedTime;
        }

        /**
         * Set the lastAccessedTime property of this {@link RateLimiterModel.IndirectReference}.
         * 
         * @param lastAccessedTime The lastAccessedTime value to set.
         */
        void setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
        }
    }
}