/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

class CountingSchedulerTest {

    ZonedDateTime time = Chrono.utc(2019, 1, 1);

    @Test
    void schedule() {
        Value v = new Value();

        CountingScheduler scheduler = new CountingScheduler();
        scheduler.schedule(() -> v.value++, 5, SECONDS);
        assert v.value == 0;

        scheduler.setTime(time);
        assert v.value == 1;

        scheduler.schedule(() -> v.value++, 5, SECONDS);
        assert v.value == 1;

        scheduler.setTime(time.plusSeconds(1));
        assert v.value == 1;

        scheduler.setTime(time.plusSeconds(3));
        assert v.value == 1;

        scheduler.setTime(time.plusSeconds(5));
        assert v.value == 2;
    }

    private static class Value {
        int value = 0;
    }
}
