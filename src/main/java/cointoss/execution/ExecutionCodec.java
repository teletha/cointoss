/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.util.Arrays;

import cointoss.Direction;
import cointoss.util.Num;
import kiss.Manageable;
import kiss.Singleton;

/**
 * {@link Execution} log writer.
 */
@Manageable(lifestyle = Singleton.class)
public class ExecutionCodec {

    /** CONSTANTS */
    private static final int ConsecutiveTypeSize = 4;

    /**
     * Build execution from log.
     * 
     * @param values
     * @return
     */
    public Execution decode(Execution previous, String[] values) {
        Execution current = new Execution();
        current.id = decodeId(values[0], previous);
        current.date = decodeDate(values[1], previous);
        current.price = decodePrice(values[2], previous);
        int value = decodeInt(values[3].charAt(0));
        if (value < ConsecutiveTypeSize) {
            current.side = Direction.BUY;
            current.consecutive = value;
        } else {
            current.side = Direction.SELL;
            current.consecutive = value - ConsecutiveTypeSize;
        }
        current.delay = decodeInt(values[3].charAt(1)) - 3;
        current.size = decodeSize(values[3].substring(2), previous);

        return current;
    }

    /**
     * Build log from execution.
     * 
     * @param execution
     * @return
     */
    public String[] encode(Execution previous, Execution execution) {
        String id = encodeId(execution, previous);
        String time = encodeDate(execution, previous);
        String price = encodePrice(execution, previous);
        String size = encodeSize(execution, previous);
        String delay = encodeInt(execution.delay + 3);
        String sideAndConsecutive = String.valueOf(execution.isBuy() ? execution.consecutive : ConsecutiveTypeSize + execution.consecutive);

        return new String[] {id, time, price, sideAndConsecutive + delay + size};
    }

    /**
     * Decode id.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected long decodeId(String value, Execution previous) {
        return Long.valueOf(value);
    }

    /**
     * Encode id.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeId(Execution execution, Execution previous) {
        return String.valueOf(execution.id);
    }

    /**
     * Decode date.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected ZonedDateTime decodeDate(String value, Execution previous) {
        return ZonedDateTime.parse(value);
    }

    /**
     * Encode date.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeDate(Execution execution, Execution previous) {
        return execution.date.toString();
    }

    /**
     * Decode size.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected Num decodeSize(String value, Execution previous) {
        return Num.of(value);
    }

    /**
     * Encode size.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodeSize(Execution execution, Execution previous) {
        return execution.size.toString();
    }

    /**
     * Decode price.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    protected Num decodePrice(String value, Execution previous) {
        return Num.of(value);
    }

    /**
     * Encode price.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    protected String encodePrice(Execution execution, Execution previous) {
        return execution.price.toString();
    }

    /**
     * Encode {@link Integer} to {@link String}.
     * 
     * @param value
     * @return
     */
    protected static String encodeInt(int value) {
        return encodeLong(value);
    }

    /**
     * Decode {@link String} to {@link Integer}.
     * 
     * @param value
     * @return
     */
    protected static int decodeInt(String value) {
        return (int) decodeLong(value);
    }

    /**
     * Decode {@link Character} to {@link Integer}.
     * 
     * @param value
     * @return
     */
    protected static int decodeInt(char value) {
        return digits[value];
    }

    /** 1byte charset. */
    private static final char[] chars = "0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~!\"#$%&'()*+,./¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
            .toCharArray();

    /** The start index. */
    protected static final int base = chars.length;

    /** The half size. */
    protected static final int half = base / 2;

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
    protected static String encodeLong(long value) {
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
    protected static long decodeLong(String value) {
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
