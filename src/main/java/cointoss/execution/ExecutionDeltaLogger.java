/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import cointoss.util.Num;

/**
 * {@link ExecutionDeltaLogger} writes compact size log by using the delta.
 */
public class ExecutionDeltaLogger extends ExecutionLogger {

    /**
     * Decode id.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    @Override
    protected long decodeId(String value, Execution previous) {
        return decodeDelta(value, previous.id, 1);
    }

    /**
     * Encode id.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    @Override
    protected String encodeId(Execution execution, Execution previous) {
        return encodeDelta(execution.id, previous.id, 1);
    }

    /**
     * Decode date.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    @Override
    protected ZonedDateTime decodeDate(String value, Execution previous) {
        return decodeDelta(value, previous.date, 0);
    }

    /**
     * Encode date.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    @Override
    protected String encodeDate(Execution execution, Execution previous) {
        return encodeDelta(execution.date, previous.date, 0);
    }

    /**
     * Decode size.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    @Override
    protected Num decodeSize(String value, Execution previous) {
        return decodeDiff(value, previous.size);
    }

    /**
     * Encode size.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    @Override
    protected String encodeSize(Execution execution, Execution previous) {
        return encodeDiff(execution.size, previous.size);
    }

    /**
     * Decode price.
     * 
     * @param value A encoded value.
     * @param previous A previous execution.
     * @return A decoded execution.
     */
    @Override
    protected Num decodePrice(String value, Execution previous) {
        return decodeDiff(value, previous.price);
    }

    /**
     * Encode price.
     * 
     * @param execution A current execution.
     * @param previous A previous execution.
     * @return An encoded value.
     */
    @Override
    protected String encodePrice(Execution execution, Execution previous) {
        return encodeDiff(execution.price, previous.price);
    }

    /**
     * Compute delta value. If it is default value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @param defaultValue A default value.
     * @return
     */
    protected static String encodeDelta(int current, int previous, int defaultValue) {
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
    protected static int decodeDelta(String current, int previous, int defaultValue) {
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
    protected static String encodeDelta(long current, long previous, long defaultValue) {
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
    protected static long decodeDelta(String current, long previous, long defaultValue) {
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
    protected static String encodeDelta(ZonedDateTime current, ZonedDateTime previous, long defaultValue) {
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
    protected static ZonedDateTime decodeDelta(String current, ZonedDateTime previous, long defaultValue) {
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
    protected static String encodeIntegralDelta(Num current, Num previous, int defaultValue) {
        int diff = current.intValue() - previous.intValue();

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
    protected static Num decodeIntegralDelta(String current, Num previous, int defaultValue) {
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
    protected static String encodeDiff(Num current, Num previous) {
        if (current.is(previous)) {
            return "";
        } else {
            int scale = current.scale();
            Num integer = current.decuple(scale);
            return encodeInt(scale + half) + encodeLong(integer.longValue());
        }
    }

    /**
     * Compute diff value. If these are same value, empty string will be returned.
     * 
     * @param current A current value.
     * @param previous A previous value.
     * @return
     */
    protected static Num decodeDiff(String current, Num previous) {
        if (current == null || current.isEmpty()) {
            return previous;
        } else {
            int scale = decodeInt(current.substring(0, 1)) - half;
            return Num.of(decodeLong(current.substring(1))).decuple(-scale);
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
    protected static String encodeDiff(Num current, Num previous, Num coefficient) {
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
    protected static Num decodeDiff(String current, Num previous, Num coefficient) {
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
    protected static String encodeDiff(String current, String previous) {
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
    protected static String decodeDiff(String current, String previous) {
        if (current == null || current.isEmpty()) {
            return previous;
        } else {
            return current;
        }
    }
}