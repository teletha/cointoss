/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import kiss.Signal;

public class DateRange {

    /** The starting date. */
    public final ZonedDateTime start;

    /** The ending date. */
    public final ZonedDateTime end;

    /** The date normalizer. */
    private final Function<ZonedDateTime, ZonedDateTime> normalizer;

    /**
     * Hide constructor.
     * 
     * @param start
     * @param end
     */
    private DateRange(ZonedDateTime start, ZonedDateTime end, Function<ZonedDateTime, ZonedDateTime> normalizer) {
        this.start = start;
        this.end = end;
        this.normalizer = normalizer;
    }

    private DateRange(DateRange range, Function<ZonedDateTime, ZonedDateTime> next) {
        this.start = next.apply(range.start);
        this.end = next.apply(range.end);
        this.normalizer = range.normalizer.andThen(next);
    }

    /**
     * Reset the starting date.
     * 
     * @param start
     * @return
     */
    public DateRange start(ZonedDateTime start) {
        return new DateRange(Chrono.min(normalizer.apply(start), end), end, normalizer);
    }

    /**
     * Reset the starting date.
     * 
     * @param time The epoch time.
     * @param unit The time unit.
     * @return
     */
    public DateRange start(long time, ChronoUnit unit) {
        return start(Chrono.utc(time, unit));
    }

    /**
     * Reset the ending date.
     * 
     * @param end
     * @return
     */
    public DateRange end(ZonedDateTime end) {
        return new DateRange(start, Chrono.max(normalizer.apply(end), start), normalizer);
    }

    /**
     * Reset the ending date.
     * 
     * @param time The epoch time.
     * @param unit The time unit.
     * @return
     */
    public DateRange end(long time, ChronoUnit unit) {
        return end(Chrono.utc(time, unit));
    }

    /**
     * Set the minimum date.
     * 
     * @param min
     * @return
     */
    public DateRange min(ZonedDateTime min) {
        ZonedDateTime m = normalizer.apply(min);
        return new DateRange(this, x -> Chrono.max(x, m));
    }

    /**
     * Set the maximum date.
     * 
     * @param max
     * @return
     */
    public DateRange max(ZonedDateTime max) {
        ZonedDateTime m = normalizer.apply(max);
        return new DateRange(this, x -> Chrono.min(x, m));
    }

    /**
     * Limit upper bounds from today.
     * 
     * @param days
     * @return
     */
    public DateRange maxByFuture(int days) {
        if (days == 0) {
            return this;
        } else {
            return max(Chrono.utcNow().plusDays(days));
        }
    }

    /**
     * Count days from start to end.
     * 
     * @return
     */
    public int countDays() {
        return countDaysFrom(start);
    }

    /**
     * Count days from the specified day to end.
     * 
     * @return
     */
    public int countDaysFrom(ZonedDateTime start) {
        return Math.abs((int) start.until(end, ChronoUnit.DAYS)) + 1;
    }

    /**
     * Count days from start to the specified day.
     * 
     * @return
     */
    public int countDaysTo(ZonedDateTime end) {
        return Math.abs((int) start.until(end, ChronoUnit.DAYS)) + 1;
    }

    /**
     * Build the date stream.
     * 
     * @return
     */
    public Signal<ZonedDateTime> days(boolean startToEnd) {
        return startToEnd ? Chrono.range(start, end) : Chrono.range(end, start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DateRange [start=" + start + ", end=" + end + "]";
    }

    /**
     * Build the normalized and sorted data range.
     * 
     * @param start
     * @param end
     * @return
     */
    public static DateRange between(ZonedDateTime start, ZonedDateTime end) {
        UnaryOperator<ZonedDateTime> normalizer = x -> Objects.requireNonNull(x).truncatedTo(ChronoUnit.DAYS);
        start = normalizer.apply(start);
        end = normalizer.apply(end);

        if (start.isBefore(end)) {
            return new DateRange(start, end, normalizer);
        } else {
            return new DateRange(end, start, normalizer);
        }
    }
}