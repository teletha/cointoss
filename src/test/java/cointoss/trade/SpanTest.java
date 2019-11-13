/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import cointoss.ticker.Span;

class SpanTest {

    @Test
    void duration() {
        assert Span.Minute1.duration.equals(Duration.ofMinutes(1));
        assert Span.Second5.duration.equals(Duration.ofSeconds(5));
        assert Span.Hour1.duration.equals(Duration.ofHours(1));
        assert Span.Day2.duration.equals(Duration.ofDays(2));
    }
}
