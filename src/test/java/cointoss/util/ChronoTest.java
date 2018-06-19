/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/06/19 19:39:00
 */
class ChronoTest {

    @Test
    void between() {
        ZonedDateTime min = ZonedDateTime.parse("2018-04-01T10:22:35.444Z");
        ZonedDateTime max = ZonedDateTime.parse("2018-04-10T10:22:35.444Z");

        ZonedDateTime before = ZonedDateTime.parse("2018-03-01T10:22:35.444Z");
        ZonedDateTime in = ZonedDateTime.parse("2018-04-05T10:22:35.444Z");
        ZonedDateTime after = ZonedDateTime.parse("2018-05-01T10:22:35.444Z");

        assert Chrono.between(min, in, max) == in;
        assert Chrono.between(min, before, max) == min;
        assert Chrono.between(min, after, max) == max;
    }
}
