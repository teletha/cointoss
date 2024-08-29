/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

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
    void betweenDurationMills() {
        Duration min = Duration.ofMillis(300);
        Duration max = Duration.ofMillis(500);

        Duration before = Duration.ofMillis(100);
        Duration in = Duration.ofMillis(400);
        Duration after = Duration.ofMillis(600);

        assert Chrono.between(min, in, max) == in;
        assert Chrono.between(min, before, max) == min;
        assert Chrono.between(min, after, max) == max;
    }

    @Test
    void epochMills() {
        assert Chrono.epochMills(ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, Chrono.UTC)) == 1000L;
        assert Chrono.epochMills(ZonedDateTime.of(2018, 4, 4, 10, 11, 14, 0, Chrono.UTC)) == 1522836674000L;
    }

    @Test
    void format() {
        assert Chrono.format(ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, Chrono.UTC), ZoneId.of("Japan")).equals("1970-01-01T09:00:01");
    }

    @Test
    void formatAsDate() {
        assert Chrono.formatAsDate(ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, Chrono.UTC), ZoneId.of("Japan")).equals("1970-01-01");
        assert Chrono.formatAsDate(ZonedDateTime.of(1970, 1, 1, 23, 0, 1, 0, Chrono.UTC), ZoneId.of("Japan")).equals("1970-01-02");
    }

    @Test
    void formatAsDuration() {
        assert Chrono.formatAsDuration(1 * 1000).equals("1");
        assert Chrono.formatAsDuration(10 * 1000).equals("10");
        assert Chrono.formatAsDuration(64 * 1000).equals("1:04");
        assert Chrono.formatAsDuration(13 * 60 * 1000).equals("13:00");
        assert Chrono.formatAsDuration(60 * 60 * 1000 + 1 * 1000).equals("1:00:01");
        assert Chrono.formatAsDuration(22 * 60 * 60 * 1000 + 12 * 1000).equals("22:00:12");
        assert Chrono.formatAsDuration(24 * 60 * 60 * 1000).equals("1:00:00:00");
        assert Chrono.formatAsDuration(11 * 24 * 60 * 60 * 1000).equals("11:00:00:00");
    }

    @Test
    void utcByMills() {
        assert Chrono.utcByMills(1000).format(Chrono.DateTime).equals("1970-01-01 00:00:01");
        assert Chrono.utcByMills(1576418285029L).format(Chrono.DateTime).equals("2019-12-15 13:58:05");
    }

    @Test
    void utcBySeconds() {
        assert Chrono.utcBySeconds(1).format(Chrono.DateTime).equals("1970-01-01 00:00:01");
        assert Chrono.utcBySeconds(1557030696L).format(Chrono.DateTime).equals("2019-05-05 04:31:36");
    }

    @Test
    void systemByMills() {
        assert Chrono.systemByMills(1000).withZoneSameInstant(Chrono.UTC).format(Chrono.DateTime).equals("1970-01-01 00:00:01");
        assert Chrono.systemByMills(1576418285029L).withZoneSameInstant(Chrono.UTC).format(Chrono.DateTime).equals("2019-12-15 13:58:05");
    }

    @Test
    void systemBySeconds() {
        assert Chrono.systemBySeconds(1).withZoneSameInstant(Chrono.UTC).format(Chrono.DateTime).equals("1970-01-01 00:00:01");
        assert Chrono.systemBySeconds(1557030696L).withZoneSameInstant(Chrono.UTC).format(Chrono.DateTime).equals("2019-05-05 04:31:36");
    }

    @Test
    void range() {
        assert Chrono.range(2020, 11).toList().size() == 30;
        assert Chrono.range(2020, 12).toList().size() == 31;
        assert Chrono.range(2021, 1).toList().size() == 31;
        assert Chrono.range(2021, 2).toList().size() == 28;
        assert Chrono.range(2021, 3).toList().size() == 31;
        assert Chrono.range(2021, 4).toList().size() == 30;
    }

    @Test
    void rangeByDate() {
        assert Chrono.range(Chrono.utc(2022, 1, 1), Chrono.utc(2022, 1, 3)).toList().size() == 3;
    }

    @Test
    void max() {
        assert Chrono.max(Chrono.utc(2021, 1, 1)).equals(Chrono.utc(2021, 1, 1));
        assert Chrono.max(Chrono.utc(2021, 1, 1), Chrono.utc(2021, 1, 2)).equals(Chrono.utc(2021, 1, 2));
        assert Chrono.max(Chrono.utc(2021, 1, 2), Chrono.utc(2021, 1, 1)).equals(Chrono.utc(2021, 1, 2));
    }

    @Test
    void min() {
        assert Chrono.min(Chrono.utc(2021, 1, 1)).equals(Chrono.utc(2021, 1, 1));
        assert Chrono.min(Chrono.utc(2021, 1, 1), Chrono.utc(2021, 1, 2)).equals(Chrono.utc(2021, 1, 1));
        assert Chrono.min(Chrono.utc(2021, 1, 2), Chrono.utc(2021, 1, 1)).equals(Chrono.utc(2021, 1, 1));
    }
}