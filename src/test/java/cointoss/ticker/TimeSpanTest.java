/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

class TimeSpanTest {

    @Test
    void calculateStartTime() {
        ZonedDateTime time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(0));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(1));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(2));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(3));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;

        time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(4));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;

        time = TimeSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(5));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;
    }

    @Test
    void calculateNextStartTime() {
        ZonedDateTime time = TimeSpan.Minute3.calculateNextStartTime(Chrono.utcNow().withMinute(0));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;
    }

    @Test
    void ticksPerDay() {
        assert TimeSpan.Day1.ticksPerDay() == 1;
        assert TimeSpan.Day2.ticksPerDay() == 1;
        assert TimeSpan.Day3.ticksPerDay() == 1;
        assert TimeSpan.Day7.ticksPerDay() == 1;
        assert TimeSpan.Hour12.ticksPerDay() == 2;
        assert TimeSpan.Hour6.ticksPerDay() == 4;
        assert TimeSpan.Hour4.ticksPerDay() == 6;
        assert TimeSpan.Hour2.ticksPerDay() == 12;
        assert TimeSpan.Hour1.ticksPerDay() == 24;
        assert TimeSpan.Minute30.ticksPerDay() == 48;
        assert TimeSpan.Minute15.ticksPerDay() == 96;
        assert TimeSpan.Minute5.ticksPerDay() == 288;
        assert TimeSpan.Minute1.ticksPerDay() == 1440;
    }
}
