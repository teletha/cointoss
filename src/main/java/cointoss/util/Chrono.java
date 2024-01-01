/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class Chrono {

    /** The UTC zone id. */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /** The system zone id. */
    public static final ZoneId SYSTEM = ZoneId.systemDefault();

    /** The minimum UTC time. */
    public static final ZonedDateTime MIN = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, UTC);

    /** Reusable format. yyyy-MM-dd */
    public static final DateTimeFormatter Date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Reusable format. yyyyMMdd */
    public static final DateTimeFormatter DateCompact = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Reusable format. yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter DateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Reusable format. yyyy-MM-dd(E) HH:mm:ss */
    public static final DateTimeFormatter DateDayTime = DateTimeFormatter.ofPattern("yyyy-MM-dd(E) HH:mm:ss");

    /** Reusable format. yyyy-MM-dd HH:mm */
    public static final DateTimeFormatter DateTimeWithoutSec = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Reusable format. yyyy-MM-dd'T'HH:mm:ss */
    public static final DateTimeFormatter DateTimeWithT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Reusable format. HH:mm:ss */
    public static final DateTimeFormatter Time = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Reusable format. HH:mm */
    public static final DateTimeFormatter TimeWithoutSec = DateTimeFormatter.ofPattern("HH:mm");

    /** Difference between the time of the NTP server and local PC. */
    private static long latestDelay;

    static {
        I.schedule(15, 15 * 60, TimeUnit.SECONDS, true).to(Chrono::measureTimeDeviation);
    }

    /**
     * Query the NTP server and measure the time deviation.
     */
    private static void measureTimeDeviation() {
        NTPUDPClient client = new NTPUDPClient();
        try {
            client.open();
            TimeInfo info = client.getTime(InetAddress.getByName("ntp.nict.jp"));
            info.computeDetails();
            latestDelay = info.getOffset();
            I.trace("Using NTP server to measure the time deviation : " + latestDelay + "ms");
        } catch (Throwable e) {
            // ignore
            I.error("A query to NTP server failed. ");
            I.error(e);
        } finally {
            client.close();
        }
    }

    /** The shared clock. */
    private static Signal<ZonedDateTime> shared = I.schedule(0, 1, TimeUnit.SECONDS, true)
            .map(v -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMills()), SYSTEM).truncatedTo(ChronoUnit.SECONDS))
            .share();

    /**
     * Gets a stream that returns the current time every second.
     * 
     * @return
     */
    public static Signal<ZonedDateTime> seconds() {
        return shared;
    }

    /**
     * Gets a stream that returns the current time every minute.
     * 
     * @return
     */
    public static Signal<ZonedDateTime> minutes() {
        return seconds().takeAt(i -> i % 60 == 0);
    }

    /**
     * Gets a stream that returns the current time every the specified minute.
     * 
     * @param minute Specify time (0 ~ 59).
     * @return
     */
    public static Signal<ZonedDateTime> minuteAt(int minute) {
        return minutes().take(time -> time.getMinute() == minute);
    }

    /**
     * Gets a stream that returns the current time every hour.
     * 
     * @return
     */
    public static Signal<ZonedDateTime> hours() {
        return seconds().takeAt(i -> i % 3600 == 0);
    }

    /**
     * Gets a stream that returns the current time every hour.
     * 
     * @param hour Specify time (0 ~ 23).
     * @return
     */
    public static Signal<ZonedDateTime> hourAt(int hour) {
        return hours().take(time -> time.getHour() == hour);
    }

    /**
     * Obtains the exact current time, corrected for the difference between the NTP server and local
     * PC time.
     * 
     * @return
     */
    public static long currentTimeMills() {
        return System.currentTimeMillis() + latestDelay;
    }

    /**
     * UTC {@link ZonedDateTime} from current time.
     * 
     * @return
     */
    public static ZonedDateTime utcNow() {
        return utcByMills(System.currentTimeMillis());
    }

    /**
     * UTC {@link ZonedDateTime}.
     * 
     * @return
     */
    public static ZonedDateTime utcToday() {
        return utcNow().truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * UTC {@link ZonedDateTime} from epoch micros.
     * 
     * @return
     */
    public static ZonedDateTime utcByMicros(long micros) {
        return Instant.EPOCH.plus(micros, ChronoUnit.MICROS).atZone(UTC);
    }

    /**
     * UTC {@link ZonedDateTime} from epoch mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime utcByMills(long mills) {
        return Instant.ofEpochMilli(mills).atZone(UTC);
    }

    /**
     * UTC {@link ZonedDateTime} from epoch seconds.
     * 
     * @return
     */
    public static ZonedDateTime utcBySeconds(long seconds) {
        return utcByMills(seconds * 1000);
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
    public static ZonedDateTime utc(int year, int month, int day, int hour, int minute, int second, int milli) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, milli * 1000000, UTC);
    }

    /**
     * UTC {@link ZonedDateTime} at the specified date.
     * 
     * @param year A year.
     * @param month A month.
     * @param day A day of month.
     * @return
     */
    public static ZonedDateTime utc(int year, int month, int day, int hour, int minute, int second, int milli, int micro) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, milli * 1000000 + micro * 1000, UTC);
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
     * UTC {@link ZonedDateTime} at the specified date.
     * 
     * @param date A target {@link LocalDate}.
     * @return
     */
    public static ZonedDateTime utc(String date) {
        try {
            return utc(LocalDate.parse(date, Date));
        } catch (DateTimeParseException e1) {
            try {
                return utc(LocalDate.parse(date, DateCompact));
            } catch (DateTimeParseException e2) {
                try {
                    return utc(LocalDateTime.parse(date, DateTime));
                } catch (DateTimeParseException e3) {
                    try {
                        return utc(LocalDateTime.parse(date, DateTimeWithoutSec));
                    } catch (DateTimeParseException e4) {
                        try {
                            return utc(LocalDateTime.parse(date, DateTimeWithT));
                        } catch (DateTimeParseException e5) {
                            try {
                                return utc(LocalDateTime.parse(date, DateDayTime));
                            } catch (DateTimeParseException e6) {
                                throw I.quiet(e6);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * System default {@link ZonedDateTime} from current time
     * 
     * @return
     */
    public static ZonedDateTime systemNow() {
        return systemByMills(System.currentTimeMillis());
    }

    /**
     * System default {@link ZonedDateTime} from epoch mills.
     * 
     * @param mills
     * @return
     */
    public static ZonedDateTime systemByMills(long mills) {
        return utcByMills(mills).withZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * System default {@link ZonedDateTime} from epoch seconds.
     * 
     * @param seconds
     * @return
     */
    public static ZonedDateTime systemBySeconds(long seconds) {
        return systemByMills(seconds * 1000);
    }

    /**
     * System default {@link ZonedDateTime} from epoch mills.
     * 
     * @return
     */
    public static ZonedDateTime system(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * Returns the maximum of the specified time.
     * 
     * @param times
     * @return
     */
    public static ZonedDateTime max(ZonedDateTime... times) {
        ZonedDateTime max = times[0];

        for (int i = 1; i < times.length; i++) {
            if (max.isBefore(times[i])) {
                max = times[i];
            }
        }
        return max;
    }

    /**
     * Returns the maximum of the specified time.
     * 
     * @param times
     * @return
     */
    public static ZonedDateTime min(ZonedDateTime... times) {
        ZonedDateTime min = times[0];

        for (int i = 1; i < times.length; i++) {
            if (min.isAfter(times[i])) {
                min = times[i];
            }
        }
        return min;
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
    public static boolean within(ZonedDateTime min, ZonedDateTime target, ZonedDateTime max) {
        return !target.isBefore(min) && !target.isAfter(max);
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
    public static boolean within(LocalDate min, LocalDate target, LocalDate max) {
        return !target.isBefore(min) && !target.isAfter(max);
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
        if (min.compareTo(target) > 0) {
            return min;
        }
        if (target.compareTo(max) < 0) {
            return target;
        }
        return max;
    }

    /**
     * List up all days at the specified month.
     * 
     * @param year
     * @param month
     * @return
     */
    public static List<ZonedDateTime> range(int year, int month) {
        ZonedDateTime current = utc(year, month, 1);
        ZonedDateTime end = current.plusMonths(1);

        List<ZonedDateTime> dates = new ArrayList();
        while (current.isBefore(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
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

    /**
     * Format the date-time to human-readable expression.
     * 
     * @param date A target date-time to format.
     * @return A formatted literal.
     */
    public static String format(ZonedDateTime date) {
        return format(date, SYSTEM);
    }

    /**
     * Format the date-time to human-readable expression.
     * 
     * @param date A target date-time to format.
     * @return A formatted literal.
     */
    public static String format(ZonedDateTime date, ZoneId zone) {
        return DateTimeWithT.format(date.withZoneSameInstant(zone));
    }

    /**
     * Format the date to human-readable expression.
     * 
     * @param date A target date to format.
     * @return A formatted literal.
     */
    public static String formatAsDate(ZonedDateTime date) {
        return formatAsDate(date, SYSTEM);
    }

    /**
     * Format the date to human-readable expression.
     * 
     * @param date A target date to format.
     * @return A formatted literal.
     */
    public static String formatAsDate(ZonedDateTime date, ZoneId zone) {
        return Date.format(date.withZoneSameInstant(zone));
    }

    /**
     * Format the date to human-readable expression.
     * 
     * @param date A target date to format.
     * @return A formatted literal.
     */
    public static String formatAsTime(ZonedDateTime date) {
        return Time.format(date.withZoneSameInstant(SYSTEM));
    }

    /**
     * Format the duration (mills) to human-readable expression.
     * 
     * @return
     */
    public static String formatAsDuration(ZonedDateTime start, ZonedDateTime end) {
        return formatAsDuration(Duration.between(start, end));
    }

    /**
     * Format the duration to human-readable expression.
     * 
     * @return
     */
    public static String formatAsDuration(Duration duration) {
        return formatAsDuration(duration.toMillis());
    }

    /**
     * Format the duration (mills) to human-readable expression.
     * 
     * @param mills
     * @return
     */
    public static String formatAsDuration(Num mills) {
        return formatAsDuration(mills.longValue());
    }

    /**
     * Format the duration (mills) to human-readable expression.
     * 
     * @param mills
     * @return
     */
    public static String formatAsDuration(long mills) {
        long seconds = mills / 1000;

        int day = 24 * 60 * 60;
        int hour = 60 * 60;
        int minute = 60;

        long days = seconds / day;
        seconds = seconds % day;
        long hours = seconds / hour;
        seconds = seconds % hour;
        long minutes = seconds / minute;
        seconds = seconds % minute;

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return "." + mills;
        } else {
            return new StringBuilder() //
                    .append(formatAsTime(days, false, true))
                    .append(formatAsTime(hours, 0 < days, true))
                    .append(formatAsTime(minutes, 0 < days || 0 < hours, true))
                    .append(formatAsTime(seconds, 0 < days || 0 < hours || 0 < minutes, false))
                    .toString();
        }
    }

    /**
     * Format as human-readable time.
     * 
     * @param value
     * @param hasPrev
     * @return
     */
    private static String formatAsTime(long value, boolean hasPrev, boolean hasNext) {
        String expression;

        if (0 == value && !hasPrev) {
            return "";
        } else {
            expression = String.valueOf(value);

            if (expression.length() == 1 && hasPrev) {
                expression = "0".concat(expression);
            }
        }
        return hasNext ? expression.concat(":") : expression;
    }

    /**
     * Pick up the one date by random.
     * 
     * @param start
     * @param end
     * @return
     */
    public static ZonedDateTime randomDate(ZonedDateTime start, ZonedDateTime end) {
        if (start.isBefore(end)) {
            return start.plusDays(RandomUtils.nextLong(0, Duration.between(start, end).toDays()));
        } else {
            return end.plusDays(RandomUtils.nextLong(0, Duration.between(end, start).toDays()));
        }
    }
}