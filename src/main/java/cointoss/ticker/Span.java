/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static java.time.temporal.ChronoField.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import kiss.I;
import kiss.Variable;

/**
 * Defined tick span.
 */
public enum Span {

    Second5(5, SECOND_OF_MINUTE, 1, HOUR_OF_DAY, 3, 1),

    Minute1(1, MINUTE_OF_HOUR, 4, HOUR_OF_DAY, 7, 1),

    Minute5(5, MINUTE_OF_HOUR, 6, HOUR_OF_DAY, 20, 1),

    Minute15(15, MINUTE_OF_HOUR, 1, EPOCH_DAY, 8, 1),

    Minute30(30, MINUTE_OF_HOUR, 1, EPOCH_DAY, 16, 1),

    Hour1(1, HOUR_OF_DAY, 1, EPOCH_DAY, 40, 1),

    Hour2(2, HOUR_OF_DAY, 1, EPOCH_DAY, 80, 1, 2),

    Hour4(4, HOUR_OF_DAY, 1, EPOCH_DAY, 160),

    Hour6(6, HOUR_OF_DAY, 1, EPOCH_DAY, 240, 1),

    Hour12(12, HOUR_OF_DAY, 1, EPOCH_DAY, 480, 1),

    Day1(1, EPOCH_DAY, 365, EPOCH_DAY, 1, 1, 2),

    Day3(3, EPOCH_DAY, 366 /* 3x122 */, EPOCH_DAY, 2),

    Day7(7, EPOCH_DAY, 364 /* 7x52 */, EPOCH_DAY, 3);

    /** The duration of this {@link Span}. */
    public final Duration duration;

    /** The duration (seconds). */
    public final long seconds;

    /** The duration (seconds) of this {@link Span}'s segment. */
    public final long segment;

    /** The maximum size of this {@link Span}'s segment. */
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
    private Span(long amount, ChronoField unit, int segment, ChronoField segmentUnit, int maximumSegmentSize, int... uppers) {
        this.amount = amount;
        this.unit = unit;
        this.duration = Duration.of(amount, unit.getBaseUnit());
        this.seconds = duration.getSeconds();
        this.segment = Duration.of(segment, segmentUnit.getBaseUnit()).toSeconds();
        this.segmentSize = maximumSegmentSize;
        this.uppers = new int[uppers.length];
        this.unitName = unit();

        for (int i = 0; i < uppers.length; i++) {
            this.uppers[i] = uppers[i] + ordinal();
        }
    }

    /**
     * Calculate the start time.
     * 
     * @param e
     */
    public ZonedDateTime calculateStartTime(ZonedDateTime time) {
        long value = time.getLong(unit);
        return time.truncatedTo(unit.getBaseUnit()).with(unit, value - (value % amount));
    }

    /**
     * Calculate the next start time.
     * 
     * @param e
     */
    public ZonedDateTime calculateNextStartTime(ZonedDateTime time) {
        return calculateStartTime(time).plus(duration);
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
}