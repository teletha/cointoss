/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.backtest;

import java.time.ZonedDateTime;

import cointoss.util.Chrono;
import cointoss.util.Generator;

/**
 * @version 2018/04/29 15:42:50
 */
public class Time {

    /** The base time */
    static final ZonedDateTime BASE = ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, Chrono.UTC);

    /** The start time */
    private final int start;

    /** The end time */
    private final int end;

    /**
     * The fixed data time.
     * 
     * @param start
     * @param end
     */
    private Time(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Convert to {@link ZonedDateTime}.
     * 
     * @return
     */
    ZonedDateTime to() {
        return BASE.plusSeconds(Generator.randomInt(start, end));
    }

    /**
     * @return
     */
    long generate() {
        return Generator.randomLong(start * 1000000000L, end * 1000000000L);
    }

    /**
     * Specify time.
     * 
     * @param seconds
     * @return
     */
    public static Time at(int seconds) {
        return new Time(seconds, seconds);
    }

    /**
     * Specify lag time.
     * 
     * @param start
     * @param end
     * @return
     */
    public static Time lag(int start, int end) {
        return new Time(start, end);
    }
}
