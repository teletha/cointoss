/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import cointoss.ticker.TimeSpan;

class SpanTest {

    @Test
    void duration() {
        assert TimeSpan.Minute1.duration.equals(Duration.ofMinutes(1));
        assert TimeSpan.Second5.duration.equals(Duration.ofSeconds(5));
        assert TimeSpan.Hour1.duration.equals(Duration.ofHours(1));
        assert TimeSpan.Day2.duration.equals(Duration.ofDays(2));
    }
}
