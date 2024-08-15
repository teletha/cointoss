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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Month;
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
    void calculateStartTimeWeek1() {
        ZonedDateTime base = Chrono.utc(2024, 1, 1, 5, 10, 21, 0); // monday

        ZonedDateTime time = Span.Week.calculateStartTime(base);
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfYear() == 1;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(1));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfYear() == 1;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(5));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 1;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(9));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 8;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(13));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 8;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(17));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 15;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(25));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 22;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(33));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 29;
        assert time.getMonth() == Month.JANUARY;

        time = Span.Week.calculateStartTime(base.plusDays(50));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 19;
        assert time.getMonth() == Month.FEBRUARY;

        time = Span.Week.calculateStartTime(base.plusDays(80));
        assert time.getMinute() == 0;
        assert time.getHour() == 0;
        assert time.getDayOfWeek() == DayOfWeek.MONDAY;
        assert time.getDayOfMonth() == 18;
        assert time.getMonth() == Month.MARCH;
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
        assert Span.Week.ticksPerDay() == 1;
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
        assert Span.Week.uppers(true).size() == 1;
        assert Span.Week.uppers(false).size() == 0;

        assert Span.Day.uppers(true).size() == 2;
        assert Span.Day.uppers(false).size() == 1;

        assert Span.Hour4.uppers(true).size() == 3;
        assert Span.Hour4.uppers(false).size() == 2;
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