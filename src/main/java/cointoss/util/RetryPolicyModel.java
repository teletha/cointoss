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
abstract class RetryPolicyModel implements WiseFunction<Signal<Throwable>, Signal<?>> {

    @VisibleForTesting
    long count;

    /**
     * Set maximum number of trials.
     * 
     * @return
     */
    @Icy.Property
    abstract long limit();

    /**
     * Set limit number to {@link Long#MAX_VALUE}.
     * 
     * @return
     */
    @Icy.Overload("limit")
    private long unlimit() {
        return Long.MAX_VALUE;
    }

    /**
     * Set the delay time between trials.
     * 
     * @return
     */
    @Icy.Property
    LongFunction<Duration> delay() {
        return i -> delayMinimum();
    }

    /**
     * Set the delay time between trials.
     * 
     * @param delay
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delay(Duration delay) {
        return i -> delay;
    }

    /**
     * Set the delay time between trials.
     * 
     * @param delay
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delay(long time, ChronoUnit unit) {
        return delay(Duration.of(time, unit));
    }

    /**
     * Set the delay time between trials.
     * 
     * @param delay
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delay(long time, TimeUnit unit) {
        return delay(time, unit.toChronoUnit());
    }

    /**
     * Set the linear delay time between trials.
     * 
     * @param baseDelay
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delayLinear(Duration baseDelay) {
        return i -> baseDelay.multipliedBy(i + 1);
    }

    /**
     * Set the minimum time to delay. The default is 0 seconds.
     * 
     * @return
     */
    @Icy.Property
    Duration delayMinimum() {
        return Duration.ZERO;
    }

    /**
     * Set the maximum time to delay. The default is 10 minutes.
     * 
     * @return
     */
    @Icy.Property
    Duration delayMaximum() {
        return Duration.of(10, MINUTES);
    }

    /**
     * Set the scheduler to manage the delay.
     * 
     * @return
     */
    @Icy.Property
    ScheduledExecutorService scheduler() {
        return null;
    }

    /**
     * Set the current number of trials to 0.
     */
    public final void reset() {
        count = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<?> APPLY(Signal<Throwable> error) throws Throwable {
        if (limit() <= 0) {
            return error.flatMap(e -> {
                return I.signalError(e);
            });
        }

        return error.flatMap(e -> {
            if (e instanceof AssertionError) {
                return I.signalError(e);
            } else {
                return I.signal(e);
            }
        }).take(limit()).delay(() -> Chrono.between(delayMinimum(), delay().apply(count++), delayMaximum()), scheduler());
    }
}
