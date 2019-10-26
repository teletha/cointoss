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

import icy.manipulator.Icy;
import kiss.I;

@Icy
abstract class APILimiterModel {

    private long storedPermits = 0;

    private long thresholdPermits;

    private long interval;

    private long latest;

    private long next;

    @Icy.Property
    public abstract int limit();

    @Icy.Property
    public abstract Duration refresh();

    @Icy.Intercept("refresh")
    private Duration config(Duration refresh) {
        thresholdPermits = limit() / 2;
        interval = refresh.toNanos() * 2 / limit();
        return refresh;
    }

    @Icy.Property
    public int express() {
        return 0;
    }

    public final void acquire() {
        long now = System.nanoTime();
        long elapsed = now - latest;
        long decreasePermits = elapsed / interval;
        storedPermits -= decreasePermits;
        if (storedPermits < 0) {
            storedPermits = 0;
        }

        long nextStoredPermits = storedPermits + 1;
        long overPermits = nextStoredPermits - thresholdPermits;

        if (overPermits <= 0) {
            storedPermits = nextStoredPermits;
            latest = now;
            next = now;
            return; // immediately
        }

        // await
        try {
            Thread.sleep(overPermits * interval / 1000000);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
        acquire();
    }
}
