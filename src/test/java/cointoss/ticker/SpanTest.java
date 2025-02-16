/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import hypatia.Num;

class SpanTest {

    @Test
    void duration() {
        assert Span.Minute1.duration.equals(Duration.ofMinutes(1));
        assert Span.Minute5.duration.equals(Duration.ofMinutes(5));
        assert Span.Hour1.duration.equals(Duration.ofHours(1));
    }

    @Test
    void calculateStartTimeMin5() {
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

        time = Span.Minute5.calculateStartTime(Chrono.utcNow().withMinute(4));
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
    void calculateStartTimeHour4() {
        ZonedDateTime time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(0));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(1));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(2));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(3));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(4));
        assert time.getMinute() == 0;
        assert time.getHour() == 4;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(5));
        assert time.getMinute() == 0;
        assert time.getHour() == 4;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(6));
        assert time.getMinute() == 0;
        assert time.getHour() == 4;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(7));
        assert time.getMinute() == 0;
        assert time.getHour() == 4;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(8));
        assert time.getMinute() == 0;
        assert time.getHour() == 8;

        time = Span.Hour4.calculateStartTime(Chrono.utcNow().withHour(9));
        assert time.getMinute() == 0;
        assert time.getHour() == 8;
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
        assert Span.Day.ticksPerDay() == 1;
        assert Span.Hour4.ticksPerDay() == 6;
        assert Span.Hour1.ticksPerDay() == 24;
        assert Span.Minute15.ticksPerDay() == 96;
        assert Span.Minute5.ticksPerDay() == 288;
        assert Span.Minute1.ticksPerDay() == 1440;
    }

    @Test
    void distance() {
        assert Span.Minute1.distance(tick(0), tick(60)) == 1;
        assert Span.Minute1.distance(tick(0), tick(120)) == 2;
        assert Span.Minute1.distance(tick(0), tick(0)) == 0;
        assert Span.Minute1.distance(tick(0), null) == 0;
        assert Span.Minute1.distance(null, tick(0)) == 0;
    }

    @Test
    void uppers() {
        assert Span.Day.uppers(true).size() == 1;
        assert Span.Day.uppers(false).size() == 0;

        assert Span.Hour4.uppers(true).size() == 2;
        assert Span.Hour4.uppers(false).size() == 1;
    }

    @Test
    void by() {
        assert Span.by("1m") == Span.Minute1;
        assert Span.by("1M") == Span.Minute1;
        assert Span.by("5m") == Span.Minute5;
        assert Span.by("15m") == Span.Minute15;
        assert Span.by("1h") == Span.Hour1;
        assert Span.by("4h") == Span.Hour4;
        assert Span.by("1d") == Span.Day;
    }

    @Test
    void near() {
        assert Span.near(0) == Span.Minute1;
        assert Span.near(30 * 1000) == Span.Minute1;
        assert Span.near(60 * 1000) == Span.Minute1;
        assert Span.near(61 * 1000) == Span.Minute5;
        assert Span.near(5 * 60 * 1000) == Span.Minute5;
        assert Span.near(10 * 60 * 1000) == Span.Minute15;
        assert Span.near(20 * 60 * 1000) == Span.Hour1;
        assert Span.near(50 * 60 * 1000) == Span.Hour1;
        assert Span.near(100 * 60 * 1000) == Span.Hour4;
        assert Span.near(200 * 60 * 1000) == Span.Hour4;
        assert Span.near(400 * 60 * 1000) == Span.Day;
    }

    /**
     * Create {@link Tick} simply.
     * 
     * @param time
     * @return
     */
    private Tick tick(int time) {
        return new Tick(time, Num.ONE, new Ticker(Span.Minute1, new TickerManager()));
    }
}