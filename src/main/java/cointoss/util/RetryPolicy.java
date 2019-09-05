/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Duration;
import java.util.function.LongFunction;

import com.google.common.annotations.VisibleForTesting;

import kiss.I;
import kiss.Signal;
import kiss.WiseFunction;

/**
 * @version 2018/07/16 9:21:49
 */
public final class RetryPolicy implements WiseFunction<Signal<Throwable>, Signal<?>> {

    /** The maximum times. */
    private long maxTimes;

    /** The minimum delay. */
    private Duration minDelay;

    /** The maximum delay. */
    private Duration maxDelay;

    /** The delay generator. */
    private LongFunction<Duration> delay;

    /** The retry counter. */
    @VisibleForTesting
    long count;

    /**
     * Default {@link RetryPolicy}.
     */
    public RetryPolicy() {
        reset();
        delay(Duration.ZERO);
        tryMaximum(Long.MAX_VALUE);
        delayMinimum(Duration.ZERO).delayMaximum(Duration.ofMinutes(30));
    }

    /**
     * Configure the maximum retry times.
     * 
     * @param times A retry times.
     * @return Chainable API.
     */
    public RetryPolicy tryMaximum(long times) {
        maxTimes = times;
        return this;
    }

    /**
     * Configure the minimum delay time.
     * 
     * @param delay A delay.
     * @return Chainable API.
     */
    public RetryPolicy delayMinimum(Duration delay) {
        if (delay != null) {
            minDelay = delay;
        }
        return this;
    }

    /**
     * Configure the maximum delay time.
     * 
     * @param delay A delay.
     * @return Chainable API.
     */
    public RetryPolicy delayMaximum(Duration delay) {
        if (delay != null) {
            maxDelay = delay;
        }
        return this;
    }

    /**
     * Configure the constant delay time.
     * 
     * @param duration A delay.
     * @return Chainable API.
     */
    public RetryPolicy delay(Duration delay) {
        if (delay != null) {
            delay(i -> delay);
        }
        return this;
    }

    /**
     * Configure the delay time.
     * 
     * @param duration A delay.
     * @return Chainable API.
     */
    public RetryPolicy delay(LongFunction<Duration> delay) {
        if (delay != null) {
            this.delay = delay;
        }
        return this;
    }

    /**
     * Configure the delay time.
     * 
     * @param duration A delay.
     * @return Chainable API.
     */
    public RetryPolicy delayLinear(Duration delay) {
        if (delay != null) {
            delay(i -> delay.multipliedBy(i));
        }
        return this;
    }

    /**
     * Configure the delay time.
     * 
     * @param duration A delay.
     * @return Chainable API.
     */
    public RetryPolicy delayExponential(Duration delay) {
        if (delay != null) {
            delay(i -> delay.multipliedBy((long) Math.pow(i, 2)));
        }
        return this;
    }

    /**
     * Reset count.
     * 
     * @return Chainable API.
     */
    public RetryPolicy reset() {
        count = 0;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<?> APPLY(Signal<Throwable> error) throws Throwable {
        if (maxTimes <= 0) {
            return error.flatMap(e -> I.signalError(e));
        }

        return error.take(() -> ++count <= maxTimes).delay(() -> Chrono.between(minDelay, delay.apply(count), maxDelay)).effect(e -> {
            System.out.println("Retry " + count + "   " + e);
            e.printStackTrace();
        });
    }
}
