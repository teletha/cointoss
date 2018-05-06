/*
 * Copyright (C) 2018 Nameless Production Committee
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
import java.util.Arrays;

import cointoss.MarketLog;

/**
 * Special codec for time serise data (i.e. {@link MarketLog}).
 * 
 * @version 2018/05/02 12:47:04
 */
public class LogCodec {

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static String encodeDelta(int current, int previous, int defaultValue) {
        int diff = current - previous;

        if (diff == defaultValue) {
            return "";
        } else {
            return encodeLong(diff);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static int decodeDelta(String current, int previous, int defaultValue) {
        if (current == null || current.isEmpty()) {
            return previous + defaultValue;
        } else {
            return previous + decodeInt(current);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static String encodeDelta(long current, long previous, long defaultValue) {
        long diff = current - previous;

        if (diff == defaultValue) {
            return "";
        } else {
            return encodeLong(diff);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static long decodeDelta(String current, long previous, long defaultValue) {
        if (current == null || current.isEmpty()) {
            return previous + defaultValue;
        } else {
            return previous + decodeLong(current);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static String encodeDelta(ZonedDateTime current, ZonedDateTime previous, long defaultValue) {
        long diff = current.toInstant().toEpochMilli() - previous.toInstant().toEpochMilli();

        if (diff == defaultValue) {
            return "";
        } else {
            return encodeLong(diff);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static ZonedDateTime decodeDelta(String current, ZonedDateTime previous, long defaultValue) {
        if (current == null || current.isEmpty()) {
            return previous.plus(defaultValue, ChronoUnit.MILLIS);
        } else {
            return previous.plus(decodeLong(current), ChronoUnit.MILLIS);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static String encodeIntegralDelta(Num current, Num previous, int defaultValue) {
        int diff = current.toInt() - previous.toInt();

        if (diff == defaultValue) {
            return "";
        } else {
            return encodeInt(diff);
        }
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    public static Num decodeIntegralDelta(String current, Num previous, int defaultValue) {
        if (current == null || current.isEmpty()) {
            return previous.plus(defaultValue);
        } else {
            return previous.plus(decodeInt(current));
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @return
     */
    public static String encodeDiff(Num current, Num previous) {
        if (current.is(previous)) {
            return "";
        } else {
            int scale = current.scale();
            Num integer = current.scaleByPowerOfTen(scale);
            return encodeInt(scale) + encodeLong(integer.toLong());
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @return
     */
    public static Num decodeDiff(String current, Num previous) {
        if (current == null || current.isEmpty()) {
            return previous;
        } else {
            int scale = decodeInt(current.substring(0, 1));
            return Num.of(decodeLong(current.substring(1))).scaleByPowerOfTen(-scale);
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param coefficient A coefficient to reduce decimal value.
     * @return
     */
    public static String encodeDiff(Num current, Num previous, Num coefficient) {
        if (current.is(previous)) {
            return "";
        } else {
            return current.multiply(coefficient).toString();
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param coefficient A coefficient to reduce decimal value.
     * @return
     */
    public static Num decodeDiff(String current, Num previous, Num coefficient) {
        if (current == null || current.isEmpty()) {
            return previous;
        } else {
            return Num.of(current).divide(coefficient);
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @return
     */
    public static String encodeDiff(String current, String previous) {
        if (current.equals(previous)) {
            return "";
        } else {
            return current;
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @return
     */
    public static String decodeDiff(String current, String previous) {
        if (current == null || current.isEmpty()) {
            return previous;
        } else {
            return current;
        }
    }

    /**
     * Encode {@link Integer} to {@link String}.
     * 
     * @param value
     * @return
     */
    public static String encodeInt(int value) {
        return encodeLong(value);
    }

    /**
     * Decode {@link String} to {@link Integer}.
     * 
     * @param value
     * @return
     */
    public static int decodeInt(String value) {
        return (int) decodeLong(value);
    }

    /**
     * Decode {@link Character} to {@link Integer}.
     * 
     * @param value
     * @return
     */
    public static int decodeInt(char value) {
        return digits[value];
    }

    /** 1byte charset. */
    private static final char[] chars = "0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~!\"#$%&'()*+,./¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
            .toCharArray();

    /** The start index. */
    private static final int base = chars.length;

    /** The pre-computed 'char to digit' mapping. */
    private static final int[] digits = new int[chars[base - 1] + 1];

    // pre-compute mapping
    static {
        Arrays.fill(digits, -1);

        for (int i = 0; i < base; i++) {
            digits[chars[i]] = i;
        }
    }

    /**
     * Encode {@link Long} to {@link String}.
     * 
     * @param value
     * @return
     */
    public static String encodeLong(long value) {
        // check zero
        if (value == 0) {
            return "0";
        }

        boolean positive = true;
        if (value < 0) {
            positive = false;
            value *= -1;
        }
        StringBuilder builder = new StringBuilder();

        while (value != 0) {
            builder.append(chars[(int) (value % base)]);
            value /= base;
        }
        return positive ? builder.toString() : "-" + builder.toString();
    }

    /**
     * Decode {@link String} to {@link Integer}.
     * 
     * @param value
     * @return
     */
    public static long decodeLong(String value) {
        boolean positive = true;
        if (value.charAt(0) == '-') {
            positive = false;
            value = value.substring(1);
        }

        long result = 0L;
        long multiplier = 1L;
        for (int position = 0; position < value.length(); position++) {
            result += multiplier * digits[value.charAt(position)];
            multiplier *= base;
        }
        return positive ? result : -result;
    }
}