/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

/**
 * @version 2018/06/30 10:25:51
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
}
