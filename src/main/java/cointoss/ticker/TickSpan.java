/*
 * Copyright (C) 2018 CoinToss Development Team
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
import java.util.List;

import kiss.I;

/**
 * Defined tick span.
 * 
 * @version 2018/01/29 9:52:28
 */
public enum TickSpan {

    /** SPAN */
    Second5(5, SECOND_OF_MINUTE),

    /** SPAN */
    Second10(10, SECOND_OF_MINUTE),

    /** SPAN */
    Second20(20, SECOND_OF_MINUTE),

    /** SPAN */
    Second30(30, SECOND_OF_MINUTE),

    /** SPAN */
    Minute1(1, MINUTE_OF_HOUR),

    /** SPAN */
    Minute2(2, MINUTE_OF_HOUR),

    /** SPAN */
    Minute3(3, MINUTE_OF_HOUR),

    /** SPAN */
    Minute5(5, MINUTE_OF_HOUR),

    /** SPAN */
    Minute10(10, MINUTE_OF_HOUR),

    /** SPAN */
    Minute15(15, MINUTE_OF_HOUR),

    /** SPAN */
    Minute20(20, MINUTE_OF_HOUR),

    /** SPAN */
    Minute30(30, MINUTE_OF_HOUR),

    /** SPAN */
    Hour1(1, HOUR_OF_DAY),

    /** SPAN */
    Hour2(2, HOUR_OF_DAY),

    /** SPAN */
    Hour3(3, HOUR_OF_DAY),

    /** SPAN */
    Hour4(4, HOUR_OF_DAY),

    /** SPAN */
    Hour5(5, HOUR_OF_DAY),

    /** SPAN */
    Hour6(6, HOUR_OF_DAY),

    /** SPAN */
    Hour12(12, HOUR_OF_DAY),

    /** SPAN */
    Day1(1, EPOCH_DAY),

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

    /** The lower units. */
    private final List<ChronoField> eraser;

    /**
     * @param amount
     * @param unit
     */
    private TickSpan(long amount, ChronoField unit) {
        List<ChronoField> units = I.list(HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE, MILLI_OF_SECOND);

        this.amount = amount;
        this.unit = unit;
        this.duration = Duration.of(amount, unit.getBaseUnit());
        this.eraser = units.subList(units.indexOf(unit) + 1, units.size());
    }

    /**
     * Calculate start time.
     * 
     * @param e
     */
    public ZonedDateTime calculateStartTime(ZonedDateTime time) {
        return time.with(temporal -> {
            for (ChronoField erase : eraser) {
                temporal = temporal.with(erase, 0);
            }
            return temporal.with(unit, temporal.getLong(unit) - (temporal.getLong(unit) % amount));
        });
    }

    /**
     * Calculate start time.
     * 
     * @param e
     */
    public ZonedDateTime calculateEndTime(ZonedDateTime time) {
        return calculateStartTime(time).plus(amount, unit.getBaseUnit());
    }
}
