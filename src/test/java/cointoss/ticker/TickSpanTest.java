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

/**
 * @version 2018/08/13 16:12:44
 */
class TickSpanTest {

    @Test
    void calculateStartTime() {
        ZonedDateTime time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(0));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(1));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(2));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 0;

        time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(3));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;

        time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(4));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;

        time = TickSpan.Minute3.calculateStartTime(Chrono.utcNow().withMinute(5));
        assert time.getSecond() == 0;
        assert time.getNano() == 0;
        assert time.getMinute() == 3;
    }

    @Test
    void ticksPerDay() {
        assert TickSpan.Day1.ticksPerDay() == 1;
        assert TickSpan.Day2.ticksPerDay() == 1;
        assert TickSpan.Day3.ticksPerDay() == 1;
        assert TickSpan.Day7.ticksPerDay() == 1;
        assert TickSpan.Hour12.ticksPerDay() == 2;
        assert TickSpan.Hour6.ticksPerDay() == 4;
        assert TickSpan.Hour4.ticksPerDay() == 6;
        assert TickSpan.Hour2.ticksPerDay() == 12;
        assert TickSpan.Hour1.ticksPerDay() == 24;
        assert TickSpan.Minute30.ticksPerDay() == 48;
        assert TickSpan.Minute15.ticksPerDay() == 96;
        assert TickSpan.Minute5.ticksPerDay() == 288;
        assert TickSpan.Minute1.ticksPerDay() == 1440;
    }
}
