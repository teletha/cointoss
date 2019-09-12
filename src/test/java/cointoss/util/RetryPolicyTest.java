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

import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class RetryPolicyTest {

    @Test
    void rety() {
        Retry rety = Retry.with.limit(3).delay(1, ChronoUnit.SECONDS);
    }

    // @Test
    // void retryMaximum() {
    // RetryPolicy policy = new RetryPolicy().retryMaximum(3);
    // assert I.signal("ok").map(new FailAction()).retryWhen(policy).to().is("OK");
    // assert policy.count == 3;
    // }
    //
    // private static class FailAction implements WiseFunction<String, String> {
    //
    // private int max = 3;
    //
    // private int current = 0;
    //
    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public String APPLY(String value) {
    // if (++current < max) {
    // throw new Error();
    // }
    // return value.toUpperCase();
    // }
    // }
    //
    // @Test
    // void delayConstants() {
    // RetryPolicy retry = new RetryPolicy().retryMaximum(2).delay(Duration.ofMillis(100));
    // Signaling<String> signal = new Signaling();
    // Variable<String> latest = signal.expose.retryWhen(retry).to();
    //
    // signal.accept("ok");
    // assert latest.is("ok");
    //
    // signal.error(new Error());
    // signal.accept("retry 1");
    // assert latest.is("retry 1");
    //
    // signal.error(new Error());
    // signal.accept("retry 2");
    // assert latest.is("retry 2");
    //
    // signal.error(new Error());
    // signal.accept("retry 3");
    // assert latest.is("retry 3");
    //
    // signal.error(new Error());
    // signal.accept("don't retry forever");
    // assert latest.is("retry 3");
    // }
}
