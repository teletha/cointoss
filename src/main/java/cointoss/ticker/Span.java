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

    Minute1(1, MINUTE_OF_HOUR, 2, HOURS, 6, 1), // 60 * 2 * 6 = 720

    Minute5(5, MINUTE_OF_HOUR, 8, HOURS, 10, 1), // 12 * 8 * 10 = 960

    Minute15(15, MINUTE_OF_HOUR, 1, DAYS, 10, 1), // 4 * 24 * 10 = 960

    Hour1(1, HOUR_OF_DAY, 4, DAYS, 10, 1, 1), // 24 * 4 * 10 = 960

    Hour4(4, HOUR_OF_DAY, 10, DAYS, 14, 1), // 6 * 10 * 14 = 840

    Day1(1, EPOCH_DAY, 60, DAYS, 14, 1), // 60 * 14 = 840

    Day7(7, EPOCH_DAY, 91 /* 7x52/4 */, DAYS, 24); // 13 * 24 =312

    /** The actual duration. */
    public final Duration duration;

    /** The actual duration (seconds). */
    public final long seconds;

    /** The duration (seconds) of the segment. */
    public final long segmentSeconds;

    /** The maximum size of the segment. */
    public final int segmentSize;

    /** The indexes of associated upper tickers. */
    final int[] uppers;

    /** The unit based amount. */
    private final long amount;

    /** The unit. */
    private final ChronoField unit;

    /** The unit name. */
    private final Variable<String> unitName;

    /**
     * @param amount
     * @param unit
     */
    private Span(long amount, ChronoField unit, int segment, ChronoUnit segmentUnit, int maximumSegmentSize, int... uppers) {
        this.amount = amount;
        this.unit = unit;
        this.duration = Duration.of(amount, unit.getBaseUnit());
        this.seconds = duration.toSeconds();
        this.segmentSeconds = Duration.of(segment, segmentUnit).toSeconds();
        this.segmentSize = maximumSegmentSize;
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
            return I.translate("days");

        case HOUR_OF_DAY:
            return I.translate("hours");

        case MINUTE_OF_HOUR:
            return I.translate("mins");

        case SECOND_OF_MINUTE:
            return I.translate("secs");

        default:
            // If this exception will be thrown, it is bug of this program. So we must rethrow
            // the
            // wrapped error in here.
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
}