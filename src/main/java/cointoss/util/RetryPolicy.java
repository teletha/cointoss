/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.Duration;
import java.util.function.LongFunction;

import kiss.Signal;
import kiss.WiseFunction;

/**
 * @version 2018/07/16 9:21:49
 */
public final class RetryPolicy implements WiseFunction<Signal<? extends Throwable>, Signal<?>> {

    /** The maximum times. */
    private long maxTimes;

    /** The minimum delay. */
    private Duration minDelay;

    /** The maximum delay. */
    private Duration maxDelay;

    /** The delay generator. */
    private LongFunction<Duration> delay;

    /** The retry counter. */
    private long count;

    /**
     * Default {@link RetryPolicy}.
     */
    public RetryPolicy() {
        reset();
        delay(Duration.ZERO);
        retryMaximum(Long.MAX_VALUE);
        delayMinimum(Duration.ZERO).delayMaximum(Duration.ofMinutes(30));
    }

    /**
     * Configure the maximum retry times.
     * 
     * @param times A retry times.
     * @return Chainable API.
     */
    public RetryPolicy retryMaximum(long times) {
        if (1 <= times) {
            maxTimes = times;
        }
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
    public Signal<?> APPLY(Signal<? extends Throwable> error) throws Throwable {
        return error.take(() -> ++count <= maxTimes).delay(() -> Chrono.between(minDelay, delay.apply(count), maxDelay)).effect(e -> {
            System.out.println("Retry " + count + "   " + e);
            e.printStackTrace();
        });
    }
}
