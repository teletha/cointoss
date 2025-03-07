/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import kiss.I;
import kiss.Variable;

/**
 * Defined tick span.
 */
public enum Span {

    Minute1("1m", 1, MINUTE_OF_HOUR, 2, HOURS, 6, 1), // 60 * 2 * 6 = 720

    /**
     * The total 5-minute time is adjusted to be within 72 hours. This is because we expect to read
     * at least 3 days of logs when the market is initialized. In this way, the need to write the
     * 5-minute ticker data to the disk cache is eliminated.
     */
    Minute5("5m", 5, MINUTE_OF_HOUR, 8, HOURS, 8, 1), // 12 * 8 * 8 = 768

    Minute15("15m", 15, MINUTE_OF_HOUR, 1, DAYS, 10, 1), // 4 * 24 * 10 = 960

    Hour1("1h", 1, HOUR_OF_DAY, 4, DAYS, 10, 1, 1), // 24 * 4 * 10 = 960

    Hour4("4h", 4, HOUR_OF_DAY, 10, DAYS, 16, 1), // 6 * 10 * 16 = 960

    Day("1d", 1, EPOCH_DAY, 60, DAYS, 30); // 60 * 30 = 1800

    /** The literal expression. */
    public final String text;

    /** The actual duration. */
    public final Duration duration;

    /** The actual duration (seconds). */
    public final long seconds;

    /** The duration (seconds) of the segment. */
    public final long segmentSeconds;

    /** The maximum size per a segment. */
    public final int itemSize;

    /** The maximum size of the segment. */
    public final int segmentSize;

    /** The extra type. */
    public final boolean sustainable;

    /** The indexes of associated upper tickers. */
    final int[] uppers;

    /** The unit based amount. */
    private final long amount;

    /** The unit. */
    private final ChronoField unit;

    /** The unit name. */
    private final Variable<String> unitName;

    private Span(String text, long amount, ChronoField unit, int segment, ChronoUnit segmentUnit, int maximumSegmentSize, int... uppers) {
        this.text = text;
        this.amount = amount;
        this.unit = unit;
        this.duration = Duration.of(amount, unit.getBaseUnit());
        this.seconds = duration.toSeconds();
        this.itemSize = segment;
        this.segmentSeconds = Duration.of(segment, segmentUnit).toSeconds();
        this.segmentSize = maximumSegmentSize;
        this.sustainable = 600 < seconds;
        this.uppers = new int[uppers.length];
        this.unitName = unit();

        for (int i = 0; i < uppers.length; i++) {
            this.uppers[i] = uppers[i] + ordinal();
        }
    }

    /**
     * Calculate the start time.
     */
    public ZonedDateTime calculateStartTime(ZonedDateTime time) {
        long value = time.getLong(unit);
        return time.truncatedTo(unit.getBaseUnit()).with(unit, value - (value % amount));
    }

    /**
     * Calculate the next start time.
     */
    public ZonedDateTime calculateNextStartTime(ZonedDateTime time) {
        return calculateStartTime(time).plus(duration);
    }

    /**
     * Compute the distance between the specified ticks.
     * 
     * @param one A target.
     * @param other A target.
     * @return
     */
    public long distance(Tick one, Tick other) {
        if (one == null || other == null) {
            return 0;
        } else {
            return Math.abs(one.openTime - other.openTime) / seconds;
        }
    }

    /**
     * Compute the previous {@link Span}.
     * 
     * @return
     */
    public Variable<Span> prev() {
        int index = ordinal();
        return index == 0 ? Variable.empty() : Variable.of(values()[index - 1]);
    }

    /**
     * Compute the next {@link Span}.
     * 
     * @return
     */
    public Variable<Span> next() {
        int index = ordinal();
        return index == values().length - 1 ? Variable.empty() : Variable.of(values()[index + 1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return amount + unitName.v;
    }

    /**
     * Compute unit expresison for each locales.
     * 
     * @param field
     * @return
     */
    private Variable<String> unit() {
        switch (unit) {
        case EPOCH_DAY:
            return I.translate("day");

        case HOUR_OF_DAY:
            return I.translate("hours");

        case MINUTE_OF_HOUR:
            return I.translate("mins");

        case SECOND_OF_MINUTE:
            return I.translate("secs");

        default:
            // If this exception will be thrown, it is bug of this program. So we must rethrow
            // the wrapped error in here.
            throw new Error();
        }
    }

    /**
     * Calculate a number of ticks per day.
     * 
     * @return A calculation result.
     */
    int ticksPerDay() {
        if (duration.toDaysPart() == 0) {
            return (int) Duration.ofDays(1).dividedBy(duration);
        } else {
            return 1;
        }
    }

    /**
     * Collect the upper spans.
     * 
     * @return
     */
    Set<Span> uppers(boolean containSelf) {
        Set<Span> set = new HashSet();
        Span[] values = Span.values();
        int key = containSelf ? ordinal() - 1 : ordinal();
        for (int i = 0; i < values.length; i++) {
            if (values[i].ordinal() > key) {
                set.add(values[i]);
            }
        }
        return set;
    }

    /**
     * Find {@link Span} by literal.
     * 
     * @param span
     * @return
     */
    public static Span by(String span) {
        span = span.toLowerCase();

        for (Span item : values()) {
            if (item.text.equals(span)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown span type : " + span);
    }

    /**
     * Find {@link Span} by duration.
     * 
     * @param mills
     * @return
     */
    public static Span near(long mills) {
        for (Span item : values()) {
            if (mills <= item.duration.toMillis()) {
                return item;
            }
        }
        return Day;
    }
}