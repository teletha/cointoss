/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DateRangeTest {

    @Test
    void range() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 4);
    }

    @Test
    void rangeReverse() {
        ZonedDateTime now = Chrono.utc(2024, 1, 4, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.minusDays(3));
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 4);
    }

    @Test
    void rangeSame() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now);
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 1);
    }

    @Test
    void start() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).start(Chrono.utc(2024, 1, 2, 4, 12, 22, 0));
        assert date(range.start, 2024, 1, 2);
        assert date(range.end, 2024, 1, 4);
    }

    @Test
    void end() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).end(Chrono.utc(2024, 1, 2, 4, 12, 22, 0));
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 2);
    }

    @Test
    void min() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).min(Chrono.utc(2024, 1, 2, 4, 12, 22, 0));
        assert date(range.start, 2024, 1, 2);
        assert date(range.end, 2024, 1, 4);
    }

    @Test
    void max() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).max(Chrono.utc(2024, 1, 2, 4, 12, 22, 0));
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 2);
    }

    @Test
    void minMax() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3))
                .min(Chrono.utc(2024, 1, 2, 5, 11, 33, 0))
                .max(Chrono.utc(2024, 1, 3, 4, 12, 22, 0));

        assert date(range.start, 2024, 1, 2);
        assert date(range.end, 2024, 1, 3);
    }

    @Test
    void startUnderMin() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).min(Chrono.utc(2024, 1, 2, 4, 12, 22, 0)).start(Chrono.utc(2023, 12, 1));
        assert date(range.start, 2024, 1, 2);
        assert date(range.end, 2024, 1, 4);
    }

    @Test
    void endOverMax() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3)).max(Chrono.utc(2024, 1, 2, 4, 12, 22, 0)).end(Chrono.utc(2024, 12, 1));
        assert date(range.start, 2024, 1, 1);
        assert date(range.end, 2024, 1, 2);
    }

    @Test
    void days() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        List<ZonedDateTime> list = range.days(true).toList();
        assert list.size() == 4;
        assert date(list.get(0), 2024, 1, 1);
        assert date(list.get(1), 2024, 1, 2);
        assert date(list.get(2), 2024, 1, 3);
        assert date(list.get(3), 2024, 1, 4);
    }

    @Test
    void daysReverse() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        List<ZonedDateTime> list = range.days(false).toList();
        assert list.size() == 4;
        assert date(list.get(0), 2024, 1, 4);
        assert date(list.get(1), 2024, 1, 3);
        assert date(list.get(2), 2024, 1, 2);
        assert date(list.get(3), 2024, 1, 1);
    }

    @Test
    void countDays() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDays() == 4;
    }

    @Test
    void countSameDay() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now);
        assert range.countDays() == 1;
    }

    @Test
    void countDaysFrom() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysFrom(Chrono.utc(2024, 1, 2)) == 3;
    }

    @Test
    void countDaysFromSameDay() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysFrom(Chrono.utc(2024, 1, 4)) == 1;
    }

    @Test
    void countDaysTo() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysTo(Chrono.utc(2024, 1, 2)) == 2;
    }

    @Test
    void countDaysToSameDay() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysTo(Chrono.utc(2024, 1, 1)) == 1;
    }

    @Test
    void countDaysFromBeforeStart() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysFrom(Chrono.utc(2023, 12, 30)) == 6;
    }

    @Test
    void countDaysFromAfterEnd() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysFrom(Chrono.utc(2024, 1, 5)) == 2;
    }

    @Test
    void countDaysToBeforeStart() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysTo(Chrono.utc(2023, 12, 30)) == 3;
    }

    @Test
    void countDaysToAfterEnd() {
        ZonedDateTime now = Chrono.utc(2024, 1, 1, 10, 10, 5, 0);
        DateRange range = DateRange.between(now, now.plusDays(3));
        assert range.countDaysTo(Chrono.utc(2024, 1, 5)) == 5;
    }

    /**
     * Date checker.
     * 
     * @param date
     * @param year
     * @param month
     * @param day
     * @return
     */
    private boolean date(ZonedDateTime date, int year, int month, int day) {
        assert date.getYear() == year;
        assert date.getMonthValue() == month;
        assert date.getDayOfMonth() == day;
        assert date.getHour() == 0;
        assert date.getMinute() == 0;
        assert date.getSecond() == 0;
        assert date.getNano() == 0;
        return true;
    }

}
