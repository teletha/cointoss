/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/07/16 11:45:36
 */
class ChronoTest {

    @Test
    void betweenZonedDateTime() {
        ZonedDateTime min = ZonedDateTime.parse("2018-04-01T10:22:35.444Z");
        ZonedDateTime max = ZonedDateTime.parse("2018-04-10T10:22:35.444Z");

        ZonedDateTime before = ZonedDateTime.parse("2018-03-01T10:22:35.444Z");
        ZonedDateTime in = ZonedDateTime.parse("2018-04-05T10:22:35.444Z");
        ZonedDateTime after = ZonedDateTime.parse("2018-05-01T10:22:35.444Z");

        assert Chrono.between(min, in, max) == in;
        assert Chrono.between(min, before, max) == min;
        assert Chrono.between(min, after, max) == max;
    }

    @Test
    void betweenDuration() {
        Duration min = Duration.ofMinutes(3);
        Duration max = Duration.ofMinutes(6);

        Duration before = Duration.ofMinutes(1);
        Duration in = Duration.ofMinutes(5);
        Duration after = Duration.ofMinutes(10);

        assert Chrono.between(min, in, max) == in;
        assert Chrono.between(min, before, max) == min;
        assert Chrono.between(min, after, max) == max;
    }

    @Test
    void epochMills() {
        assert Chrono.epochMills(ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, Chrono.UTC)) == 1000L;
        assert Chrono.epochMills(ZonedDateTime.of(2018, 4, 4, 10, 11, 14, 0, Chrono.UTC)) == 1522836674000L;
    }
}
