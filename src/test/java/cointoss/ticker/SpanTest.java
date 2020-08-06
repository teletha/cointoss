/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

class SpanTest {

    @Test
    void duration() {
        assert Span.Minute1.duration.equals(Duration.ofMinutes(1));
        assert Span.Minute5.duration.equals(Duration.ofMinutes(5));
        assert Span.Hour1.duration.equals(Duration.ofHours(1));
    }

    @Test
    void calculateStartTime() {
        ZonedDateTime time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(0));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(1));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(2));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(3));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(5));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 5;

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(6));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 5;
    }

    @Test
    void calculateNextStartTime() {
        ZonedDateTime time = Span.Minute5.calculateNextStartTime(Chrono.utcNow().withMinute(0));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 5;
    }

    @Test
    void ticksPerDay() {
        assert Span.Day1.ticksPerDay() == 1;
        assert Span.Day3.ticksPerDay() == 1;
        assert Span.Day7.ticksPerDay() == 1;
        assert Span.Hour12.ticksPerDay() == 2;
        assert Span.Hour6.ticksPerDay() == 4;
        assert Span.Hour4.ticksPerDay() == 6;
        assert Span.Hour2.ticksPerDay() == 12;
        assert Span.Hour1.ticksPerDay() == 24;
        assert Span.Minute30.ticksPerDay() == 48;
        assert Span.Minute15.ticksPerDay() == 96;
        assert Span.Minute5.ticksPerDay() == 288;
        assert Span.Minute1.ticksPerDay() == 1440;
    }
}