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

import static java.util.concurrent.TimeUnit.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import antibug.Chronus;
import kiss.I;
import kiss.Observer;
import kiss.WiseFunction;
import kiss.WiseRunnable;

class RetryPolicyTest {

    Chronus chronus = new Chronus();

    WiseFunction<Integer, String> alwaysFail = num -> {
        Thread.sleep(1);
        throw new Error("Failed Number " + num);
    };

    @Test
    void limit() {
        RetryPolicy policy = RetryPolicy.with.limit(3);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to((Observer) result);
        assert policy.count == 3;
        assert result.hasOnlyError("Failed Number 1");
    }

    @Test
    void delayFixedDuration() {
        Result result = new Result();
        RetryPolicy policy = RetryPolicy.with.limit(3).delay(100, MILLISECONDS).scheduler(chronus).autoReset(false);
        policy.onRetry = result;

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to((Observer) result);
        chronus.await();
        assert policy.count == 3;
        assert result.hasOnlyError("Failed Number 1");
        assert result.checkMinimumRequiredInterval(100, 100);
    }

    @Test
    void delayLinearDuration() {
        Result result = new Result();
        RetryPolicy policy = RetryPolicy.with.limit(5).delayLinear(Duration.ofMillis(30)).scheduler(chronus).autoReset(false);
        policy.onRetry = result;

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to((Observer) result);
        chronus.await();
        assert policy.count == 5;
        assert result.hasOnlyError("Failed Number 1");
        assert result.checkMinimumRequiredInterval(30, 60, 90, 120);
    }

    @Test
    void delayMinimumDuration() {
        Result result = new Result();
        RetryPolicy policy = RetryPolicy.with.limit(5)
                .delayLinear(Duration.ofMillis(20))
                .delayMinimum(Duration.ofMillis(50))
                .scheduler(chronus)
                .autoReset(false);
        policy.onRetry = result;

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to((Observer) result);
        chronus.await();
        assert policy.count == 5;
        assert result.hasOnlyError("Failed Number 1");
        assert result.checkMinimumRequiredInterval(50, 50, 60, 80);
    }

    @Test
    void delayMaximumDuration() {
        Result result = new Result();
        RetryPolicy policy = RetryPolicy.with.limit(5)
                .delayLinear(Duration.ofMillis(20))
                .delayMaximum(Duration.ofMillis(50))
                .scheduler(chronus)
                .autoReset(false);
        policy.onRetry = result;

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to((Observer) result);
        chronus.await();
        assert policy.count == 5;
        assert result.hasOnlyError("Failed Number 1");
        assert result.checkMinimumRequiredInterval(20, 40, 50, 50);
    }

    /**
     * 
     */
    private static class Result implements Observer<String>, WiseRunnable {

        private List<String> values = new ArrayList();

        private List<Throwable> errors = new ArrayList();

        private long startTime = System.nanoTime();

        private List<Long> retryTiming = new ArrayList();

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(String value) {
            this.values.add(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable e) {
            errors.add(e);
        }

        /**
         * Test error message.
         * 
         * @param message
         * @return
         */
        private boolean hasOnlyError(String message) {
            assert errors.size() == 1;
            assert errors.get(0).getMessage().equals(message);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void RUN() throws Throwable {
            retryTiming.add((System.nanoTime() - startTime) / 1000000);
        }

        /**
         * Check executed timing by intervals.
         * 
         * @param intervals
         * @return
         */
        private boolean checkMinimumRequiredInterval(int... intervals) {
            // assert intervals.length + 1 == retryTiming.size();

            for (int i = 0; i < retryTiming.size() - 1; i++) {
                assert intervals[i] <= retryTiming.get(i + 1) - retryTiming
                        .get(i) : "Interval: " + intervals[i] + " Timings: " + retryTiming;
            }
            return true;
        }
    }
}
