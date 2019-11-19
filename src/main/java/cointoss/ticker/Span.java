/*
 * Copyright (C) 2019 CoinToss Development Team
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

/**
 * Defined tick span.
 */
public enum Span {

    /** SPAN */
    Second5(5, SECOND_OF_MINUTE, 1),

    /** SPAN */
    Second15(15, SECOND_OF_MINUTE, 1),

    /** SPAN */
    Second30(30, SECOND_OF_MINUTE, 1),

    /** SPAN */
    Minute1(1, MINUTE_OF_HOUR, 1, 2),

    /** SPAN */
    Minute3(3, MINUTE_OF_HOUR),

    /** SPAN */
    Minute5(5, MINUTE_OF_HOUR, 1, 2),

    /** SPAN */
    Minute10(10, MINUTE_OF_HOUR),

    /** SPAN */
    Minute15(15, MINUTE_OF_HOUR, 1),

    /** SPAN */
    Minute30(30, MINUTE_OF_HOUR, 1),

    /** SPAN */
    Hour1(1, HOUR_OF_DAY, 1),

    /** SPAN */
    Hour2(2, HOUR_OF_DAY, 1, 2),

    /** SPAN */
    Hour4(4, HOUR_OF_DAY),

    /** SPAN */
    Hour6(6, HOUR_OF_DAY, 1),

    /** SPAN */
    Hour12(12, HOUR_OF_DAY, 1),

    /** SPAN */
    Day1(1, EPOCH_DAY, 1, 2, 3),

    /** SPAN */
    Day2(2, EPOCH_DAY),

    /** SPAN */
    Day3(3, EPOCH_DAY),

    /** SPAN */
    Day7(7, EPOCH_DAY);

    /** The duration. */
    public final Duration duration;

    /** The unit based amount. */
    private final long amount;

    /** The unit. */
    private final ChronoField unit;

    /** The indexes of associated upper tickers. */
    final int[] uppers;

    /**
     * @param amount
     * @param unit
     */
    private Span(long amount, ChronoField unit, int... uppers) {
        this.amount = amount;
        this.unit = unit;
        this.duration = Duration.of(amount, unit.getBaseUnit());
        this.uppers = new int[uppers.length];

        for (int i = 0; i < uppers.length; i++) {
            this.uppers[i] = uppers[i] + ordinal();
        }
    }

    /**
     * Calculate start time.
     * 
     * @param e
     */
    public ZonedDateTime calculateStartTime(ZonedDateTime time) {
        long value = time.getLong(unit);
        return time.truncatedTo(unit.getBaseUnit()).with(unit, value - (value % amount));
    }

    /**
     * Calculate a number of ticks per day.
     * 
     * @return A calculation result.
     */
    public int ticksPerDay() {
        if (duration.toDaysPart() == 0) {
            return (int) Duration.ofDays(1).dividedBy(duration);
        } else {
            return 1;
        }
    }
}