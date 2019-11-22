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

class RetryPolicyTest {

    Chronus chronus = new Chronus();

    WiseFunction<Integer, String> alwaysFail = num -> {
        throw new Error("Failed Number " + num);
    };

    @Test
    void limit() {
        RetryPolicy policy = RetryPolicy.with.limit(3);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to(result);
        assert policy.count == 3;
        assert result.hasOnlyError("Failed Number 1");
    }

    @Test
    void delayFixedDuration() {
        RetryPolicy policy = RetryPolicy.with.limit(3).delay(100, MILLISECONDS).scheduler(chronus);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to(result);
        assert policy.count == 1;
        assert result.hasNoError();
        chronus.await(120, MILLISECONDS);
        assert policy.count == 2;
        assert result.hasNoError();
        chronus.await(120, MILLISECONDS);
        assert policy.count == 3;
        assert result.hasNoError();
        chronus.await();
        assert result.hasOnlyError("Failed Number 1");
    }

    @Test
    void delayLinearDuration() {
        RetryPolicy policy = RetryPolicy.with.limit(3).delayLinear(Duration.ofMillis(30)).scheduler(chronus);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to(result);
        assert policy.count == 1;
        chronus.await(35, MILLISECONDS); // wait 35ms then second try
        assert policy.count == 2;
        chronus.await(40, MILLISECONDS); // 40ms is not enough
        assert policy.count == 2;
        chronus.await(30, MILLISECONDS); // wait 70ms then third try
        assert policy.count == 3;
        chronus.await(80, MILLISECONDS); // 80ms is not enough
        assert policy.count == 3;
        chronus.await(20, MILLISECONDS); // wait 100ms then fail
        assert result.hasOnlyError("Failed Number 1");
    }

    @Test
    void delayMinimumDuration() {
        RetryPolicy policy = RetryPolicy.with.limit(3)
                .delayLinear(Duration.ofMillis(30))
                .delayMinimum(Duration.ofMillis(60))
                .scheduler(chronus);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to(result);
        assert policy.count == 1;
        chronus.await(35, MILLISECONDS); // 35ms is not enough
        assert policy.count == 1;
        chronus.await(35, MILLISECONDS); // wait 70ms then second try
        assert policy.count == 2;
        chronus.await(50, MILLISECONDS); // 50ms is not enough
        assert policy.count == 2;
        chronus.await(20, MILLISECONDS); // wait 70ms then third try
        assert policy.count == 3;
        assert result.hasNoError();
        chronus.await();
        assert result.hasOnlyError("Failed Number 1");
    }

    @Test
    void delayMaximumDuration() {
        RetryPolicy policy = RetryPolicy.with.limit(4)
                .delayLinear(Duration.ofMillis(30))
                .delayMaximum(Duration.ofMillis(50))
                .scheduler(chronus);
        Result result = new Result();

        I.signal(1, 2, 3, 4, 5).map(alwaysFail).retryWhen(policy).to(result);
        assert policy.count == 1;
        chronus.await(35, MILLISECONDS); // wait 35ms then second try
        assert policy.count == 2;
        chronus.await(80, MILLISECONDS); // wait then third try
        assert policy.count == 3;
        chronus.await(80, MILLISECONDS); // wait then forth try
        assert policy.count == 4;
        chronus.await();
        assert result.hasOnlyError("Failed Number 1");
    }

    /**
     * 
     */
    private static class Result implements Observer<String> {

        private List<String> values = new ArrayList();

        private List<Throwable> errors = new ArrayList();

        private boolean complete;

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
            complete = true;
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
            Throwable e = errors.get(0);
            assert e.getMessage().equals(message);
            return true;
        }

        /**
         * Check error existance
         * 
         * @return
         */
        private boolean hasNoError() {
            assert errors.isEmpty();
            return true;
        }
    }
}
