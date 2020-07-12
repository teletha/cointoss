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

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import icy.manipulator.Icy;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.WiseFunction;
import kiss.WiseRunnable;

@Icy
abstract class RetryPolicyModel implements WiseFunction<Signal<Throwable>, Signal<?>> {

    private static final Logger logger = LogManager.getLogger();

    @VisibleForTesting
    long count;

    @VisibleForTesting
    boolean parking;

    Disposable autoReset;

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
     * Set the scheduler to manage the delay.
     * 
     * @return
     */
    @Icy.Property
    boolean autoReset() {
        return true;
    }

    /**
     * Ignore the specified error types.
     * 
     * @return
     */
    @Icy.Property
    List<Class<? extends Throwable>> ignore() {
        return List.of();
    }

    /**
     * Show debuggable message with the specified name.
     * 
     * @return
     */
    @Icy.Property
    String debug() {
        return "";
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
    public String toString() {
        return "RetryPolicy[" + debug() + " @" + count + "]";
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
            }

            for (Class<? extends Throwable> type : ignore()) {
                if (type.isInstance(e)) {
                    return I.signalError(e);
                }
            }

            if (parking) {
                return I.signal();
            } else {
                parking = true;
                return I.signal(e);
            }
        }).take(limit()).delay(e -> {
            if (autoReset()) {
                if (autoReset != null) {
                    autoReset.dispose();
                }
                autoReset = I.schedule(delayMaximum().toMillis() * 2, TimeUnit.MILLISECONDS, scheduler()).to(this::reset);
            }

            Duration duration = Chrono.between(delayMinimum(), delay().apply(count++), delayMaximum());

            String debug = debug();
            if (debug != null && debug.length() != 0) {
                logger.info(this + " will retry after " + Chrono.formatAsDuration(duration) + "\t: " + e);
                // e.printStackTrace();
            }

            return duration;
        }, scheduler()).effect(() -> parking = false).effect(onRetry);
    }
}