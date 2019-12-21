/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
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
}
