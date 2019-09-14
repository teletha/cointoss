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

import static java.time.temporal.ChronoUnit.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

import com.google.common.annotations.VisibleForTesting;

import icy.manipulator.Icy;
import kiss.I;
import kiss.Signal;
import kiss.WiseFunction;

@Icy
public abstract class RetryModel implements WiseFunction<Signal<Throwable>, Signal<?>> {

    @VisibleForTesting
    long count;

    @Icy.Property
    abstract long limit();

    @Icy.Overload("limit")
    private long unlimit() {
        return Long.MAX_VALUE;
    }

    @Icy.Property
    LongFunction<Duration> delay() {
        return i -> delayMinimum();
    }

    @Icy.Overload("delay")
    private LongFunction<Duration> delay(Duration delay) {
        return i -> delay;
    }

    @Icy.Overload("delay")
    private LongFunction<Duration> delay(long time, ChronoUnit unit) {
        return delay(Duration.of(time, unit));
    }

    @Icy.Overload("delay")
    private LongFunction<Duration> delay(long time, TimeUnit unit) {
        return delay(time, unit.toChronoUnit());
    }

    @Icy.Overload("delay")
    private LongFunction<Duration> delayLinear(Duration delay) {
        return i -> delay.multipliedBy(i);
    }

    @Icy.Overload("delay")
    private LongFunction<Duration> delayExponential(Duration delay) {
        return i -> delay.multipliedBy((long) Math.pow(i, 2));
    }

    @Icy.Property
    Duration delayMinimum() {
        return Duration.ZERO;
    }

    @Icy.Property
    Duration delayMaximum() {
        return Duration.of(30, MINUTES);
    }

    @Icy.Property
    ScheduledExecutorService scheduler() {
        return null;
    }

    /**
     * Reset count.
     */
    public void reset() {
        count = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<?> APPLY(Signal<Throwable> error) throws Throwable {
        if (limit() <= 0) {
            return error.flatMap(e -> I.signalError(e));
        }
        return error.take(limit()).delay(() -> Chrono.between(delayMinimum(), delay().apply(count++), delayMaximum()), scheduler());
    }
}
