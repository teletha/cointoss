/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

/**
 * @version 2018/08/03 8:39:27
 */
public class Chrono {

    /** The UTC zone id. */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /** The minimum UTC time. */
    public static final ZonedDateTime MIN = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, UTC);

    /** Reusable format. yyyy-MM-dd */
    public static final DateTimeFormatter Date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Reusable format. yyyyMMdd */
    public static final DateTimeFormatter DateCompact = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Reusable format. yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter DateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Reusable format. yyyy-MM-dd'T'HH:mm:ss */
    public static final DateTimeFormatter DateTimeWithoutSec = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Reusable format. yyyy-MM-dd'T'HH:mm:ss */
    public static final DateTimeFormatter DateTimeWithT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Reusable format. HH:mm:ss */
    public static final DateTimeFormatter Time = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Reusable format. HH:mm */
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
     * @param date A target {@link LocalDateTime}.
     * @return
     */
    public static ZonedDateTime utc(LocalDateTime date) {
        return date.atZone(UTC);
    }

    /**
     * UTC {@link ZonedDateTime} at the specified date.
     * 
     * @param date A target {@link LocalDate}.
     * @return
     */
    public static ZonedDateTime utc(LocalDate date) {
        return utc(date.atTime(0, 0));
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
     * Ensures that the specified date is between the minimum and maximum date. If the specified
     * date is out of range, the closest date is returned.
     * 
     * @param min A minimum date.
     * @param target A target date.
     * @param max A maximum date.
     * @return
     */
    public static ZonedDateTime between(ZonedDateTime min, ZonedDateTime target, ZonedDateTime max) {
        return target.isBefore(min) ? min : target.isAfter(max) ? max : target;
    }

    /**
     * Ensures that the specified duration is between the minimum and maximum duration. If the
     * specified duration is out of range, the closest duration is returned.
     * 
     * @param min A minimum duration.
     * @param target A target duration.
     * @param max A maximum duration.
     * @return
     */
    public static Duration between(Duration min, Duration target, Duration max) {
        if (min.compareTo(target) == 1) {
            return min;
        }
        if (target.compareTo(max) == -1) {
            return target;
        }
        return max;
    }

    /**
     * Compute epoch millseconds of the specified date.
     * 
     * @param date A target date.
     * @return
     */
    public static long epochMills(Temporal date) {
        return date.getLong(ChronoField.INSTANT_SECONDS) * 1000 + date.getLong(ChronoField.MILLI_OF_SECOND);
    }
}
