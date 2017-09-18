/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import cointoss.Execution;

/**
 * @version 2017/09/03 22:04:28
 */
public class Span {

    /** The zero length */
    public static final Span ZERO = new Span(ZonedDateTime.now(), ZonedDateTime.now());

    /** The duration format. */
    private static final DateTimeFormatter durationHMS = DateTimeFormatter.ofPattern("MM/dd' 'HH:mm:ss");

    /** The start date. */
    public final ZonedDateTime start;

    /** The end date. */
    public final ZonedDateTime end;

    /**
     * @param start
     * @param end
     */
    public Span(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        this.start = ZonedDateTime.of(startYear, startMonth, startDay, 0, 0, 0, 0, Execution.UTC);
        this.end = ZonedDateTime.of(endYear, endMonth, endDay, 0, 0, 0, 0, Execution.UTC);
    }

    /**
     * @param start
     * @param end
     */
    public Span(ZonedDateTime start, ZonedDateTime end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Calculate duration seconds.
     * 
     * @return
     */
    public long time() {
        return end.toEpochSecond() - start.toEpochSecond();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append(durationHMS.format(start))
                .append("～")
                .append(start.isEqual(end) ? "\t\t" : durationHMS.format(end))
                .toString();
    }

    /**
     * @param start
     * @param end
     * @param size
     * @return
     */
    public static Span random(ZonedDateTime start, ZonedDateTime end, int size) {
        long days = ChronoUnit.DAYS.between(start, end.minusDays(size));
        long offset = Generator.randomLong(0, days);
        return new Span(start.plusDays(offset), start.plusDays(offset + size));
    }
}
