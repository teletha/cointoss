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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @version 2018/02/15 14:56:42
 */
public class Chrono {

    /** The UTC zone id. */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /** Reusable format. */
    public static final DateTimeFormatter Date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
}
