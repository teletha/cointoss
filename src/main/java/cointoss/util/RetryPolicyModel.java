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
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

import com.google.common.annotations.VisibleForTesting;

import cointoss.util.NetworkError.Kind;
import icy.manipulator.Icy;
import kiss.I;
import kiss.Signal;
import kiss.WiseFunction;
import kiss.WiseRunnable;

@Icy
abstract class RetryPolicyModel implements WiseFunction<Signal<Throwable>, Signal<?>> {

    @VisibleForTesting
    long count;

    @VisibleForTesting
    boolean parking;

    long latestRetryTime;

    @VisibleForTesting
    WiseRunnable onRetry;

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
        return i -> Duration.ZERO;
    }

    /**
     * Set the delay time between trials.
     * 
     * @return
     */
    @Icy.Property
    LongFunction<Duration> delayOnLimitOverflow() {
        return i -> Duration.ofMinutes(1);
    }

    /**
     * Set the delay time between trials.
     * 
     * @return
     */
    @Icy.Property
    LongFunction<Duration> delayOnMaintenace() {
        return i -> Duration.ofMinutes(10);
    }

    /**
     * Set the delay time between trials.
     * 
     * @param time A delay time on retry.
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delay(Duration time) {
        return i -> time;
    }

    /**
     * Set the delay time between trials.
     * 
     * @param time A delay time on retry.
     * @param unit A delay time unit on retry.
     * @return
     */
    @Icy.Overload("delay")
    private LongFunction<Duration> delay(long time, ChronoUnit unit) {
        return delay(Duration.of(time, unit));
    }

    /**
     * Set the delay time between trials.
     * 
     * @param time A delay time on retry.
     * @param unit A delay time unit on retry.
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
     * Show debuggable name with the specified name.
     * 
     * @return
     */
    @Icy.Property
    String name() {
        return "";
    }

    /**
     * Set the name of this policy.
     * 
     * @return
     */
    @Icy.Property
    ScheduledExecutorService scheduler() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "RetryPolicy[" + name() + " @" + count + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<?> APPLY(Signal<Throwable> error) throws Throwable {
        return error.flatMap(e -> limit() <= 0 || !isRecoverableError(e) ? I.signalError(e) : I.signal(e)).take(limit()).delay(e -> {
            // try to reset counting
            long now = System.currentTimeMillis();
            if (now - latestRetryTime > 10 * 60 * 1000) {
                count = 0;
            }
            latestRetryTime = now;

            Duration duration = Duration.ZERO;
            LongFunction<Duration> delay = NetworkError.check(e, Kind.Maintenance) ? delayOnMaintenace()
                    : NetworkError.check(e, Kind.LimitOverflow) ? delayOnLimitOverflow() : delay();
            if (delay != null) {
                duration = delay.apply(count++);
                if (10 < duration.toMinutes()) {
                    duration = Duration.ofMinutes(10);
                }
            }

            String name = name();
            if (name != null && name.length() != 0) {
                I.error(this + " will retry after " + Chrono.formatAsDuration(duration) + ".");
                I.error(e);
            }

            return duration;
        }, scheduler()).effect(onRetry);
    }

    private boolean isRecoverableError(Throwable e) {
        if (e instanceof AssertionError) {
            return false;
        }

        if (e instanceof NetworkError x) {
            return x.kind.recoverable;
        }
        return true;
    }

    public static WiseFunction<Signal<Throwable>, Signal<?>> comply(long limit) {
        return comply(limit, null);
    }

    public static WiseFunction<Signal<Throwable>, Signal<?>> comply(long limit, LongFunction<Duration> delay) {
        return comply(limit, delay, null);
    }

    public static WiseFunction<Signal<Throwable>, Signal<?>> comply(long limit, LongFunction<Duration> delay, ScheduledExecutorService scheduler) {
        long[] c = {0, 0};

        return x -> x.flatMap(e -> limit <= 0 || e instanceof AssertionError ? I.signalError(e) : I.signal(e)).take(limit).delay(e -> {
            long now = System.currentTimeMillis();
            if (now - c[1] > 10 * 60 * 1000) {
                c[0] = 0;
            }
            c[1] = now;

            if (delay == null) {
                return Duration.ZERO;
            } else {
                Duration duration = delay.apply(c[0]++);
                if (10 < duration.toMinutes()) {
                    duration = Duration.ofMinutes(10);
                }
                return duration;
            }
        }, scheduler);
    }
}