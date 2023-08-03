/*
 * Copyright (C) 2023 The COINTOSS Development Team
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
import java.util.concurrent.TimeUnit;

public interface TimebaseSupport {

    /**
     * Gets the reference current time.
     * 
     * @return
     */
    default ZonedDateTime baseTime() {
        return Chrono.MIN;
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @param unit
     * @return
     */
    default ZonedDateTime after(int time, TimeUnit unit) {
        return after(time, unit.toChronoUnit());
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @param unit
     * @return
     */
    default ZonedDateTime after(int time, ChronoUnit unit) {
        return baseTime().plus(time, unit);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterMilli(int time) {
        return after(time, ChronoUnit.MILLIS);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterSecond(int time) {
        return after(time, ChronoUnit.SECONDS);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterMinute(int time) {
        return after(time, ChronoUnit.MINUTES);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterHour(int time) {
        return after(time, ChronoUnit.HOURS);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterDay(int time) {
        return after(time, ChronoUnit.DAYS);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterWeek(int time) {
        return after(time, ChronoUnit.WEEKS);
    }

    /**
     * Gets the time when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default ZonedDateTime afterMonth(int time) {
        return after(time, ChronoUnit.MONTHS);
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterSecond(int time) {
        return afterSecond(time).toEpochSecond();
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterMinute(int time) {
        return afterMinute(time).toEpochSecond();
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterHour(int time) {
        return afterHour(time).toEpochSecond();
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterDay(int time) {
        return afterDay(time).toEpochSecond();
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterWeek(int time) {
        return afterWeek(time).toEpochSecond();
    }

    /**
     * Gets the epoch seconds when the specified time has elapsed from the reference current time.
     * 
     * @param time
     * @return
     */
    default long epochAfterMonth(int time) {
        return afterMonth(time).toEpochSecond();
    }
}