/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @version 2018/05/09 13:24:12
 */
public class Chrono {

    /** The UTC zone id. */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /** Reusable format. */
    public static final DateTimeFormatter Date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Reusable format. */
    public static final DateTimeFormatter DateCompact = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Reusable format. */
    public static final DateTimeFormatter DateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Reusable format. */
    public static final DateTimeFormatter DateTimeWithT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Reusable format. */
    public static final DateTimeFormatter Time = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Reusable format. */
    public static final DateTimeFormatter TimeWithoutSec = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * UTC {@link ZonedDateTime} from current time.
     * 
     * @return
     */
    public static ZonedDateTime utcNow() {
        return utc(System.currentTimeMillis());
    }

    /**
     * UTC {@link ZonedDateTime} from epoc mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime utc(long mills) {
        return Instant.ofEpochMilli(mills).atZone(UTC);
    }

    /**
     * UTC {@link ZonedDateTime} from epoc mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime utc(double mills) {
        return utc((long) mills);
    }

    /**
     * UTC {@link ZonedDateTime} at the specified date.
     * 
     * @param year A year.
     * @param month A month.
     * @param day A day of month.
     * @return
     */
    public static ZonedDateTime utc(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, UTC);
    }

    /**
     * UTC {@link ZonedDateTime} at the specified date.
     * 
     * @param year A year.
     * @param month A month.
     * @param day A day of month.
     * @return
     */
    public static ZonedDateTime utf(LocalDate date) {
        return date.atTime(0, 0).atZone(UTC);
    }

    /**
     * System default {@link ZonedDateTime} from current time
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime systemNow() {
        return systemByMills(System.currentTimeMillis());
    }

    /**
     * System default {@link ZonedDateTime} from epoc mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime systemByMills(long mills) {
        return utc(mills).withZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * System default {@link ZonedDateTime} from epoc mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime systemByMills(double mills) {
        return systemByMills((long) mills);
    }

    /**
     * System default {@link ZonedDateTime} from epoc seconds.
     * 
     * @param seconds
     * @return
     */
    public static ZonedDateTime systemBySeconds(long seconds) {
        return systemByMills(seconds * 1000);
    }

    /**
     * System default {@link ZonedDateTime} from epoc seconds.
     * 
     * @param seconds
     * @return
     */
    public static ZonedDateTime systemBySeconds(double seconds) {
        return systemBySeconds((long) seconds);
    }

    /**
     * System default {@link ZonedDateTime} from epoc mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime system(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * Ensures that the specified date is between the minimum and maximum date.If the specified date
     * is out of range, the closest date is returned.
     * 
     * @param min A minimum date.
     * @param target A target date.
     * @param max A maximum date.
     * @return
     */
    public static ZonedDateTime between(ZonedDateTime min, ZonedDateTime target, ZonedDateTime max) {
        return target.isBefore(min) ? min : target.isAfter(max) ? max : target;
    }
}
