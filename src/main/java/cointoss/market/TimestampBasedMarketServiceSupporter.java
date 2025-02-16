/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import static cointoss.Direction.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import hypatia.Num;
import kiss.JSON;

/**
 * This class is useful to use in the following cases
 * <ul>
 * <li>The execution ID obtained by the REST API and the Websocket API are not the same.</li>
 * <li>The REST or Websocket API or both do not contain the execution ID.</li>
 * <li>REST API cannot obtain the necessary execution information and CandleStick is used
 * instead.</li>
 * </ul>
 */
public class TimestampBasedMarketServiceSupporter {

    /** The padding to avoid equating multiple trades occurring at the same time. */
    public final long padding;

    private final boolean milliBase;

    /**
     * Construct with millisecond based padding (10000).
     */
    public TimestampBasedMarketServiceSupporter() {
        this(10000);
    }

    /**
     * Construct with millisecond based padding.
     * 
     * @param padding A padding size.
     */
    public TimestampBasedMarketServiceSupporter(long padding) {
        this(padding, true);
    }

    /**
     * Construct with your padding.
     * 
     * @param padding A padding size.
     * @param milliBase A timestamp base.
     */
    public TimestampBasedMarketServiceSupporter(long padding, boolean milliBase) {
        if (padding <= 0) {
            throw new IllegalArgumentException("Padding size must be positive.");
        }

        this.padding = padding;
        this.milliBase = milliBase;
    }

    /**
     * Convert from id to epoch time.
     * 
     * @param id A target ID.
     * @return Epoch time.
     */
    public final long computeEpochTime(long id) {
        return id / padding;
    }

    /**
     * Convert from id to epoch second.
     * 
     * @param id A target ID.
     * @return Epoch second.
     */
    public final long computeEpochSecond(long id) {
        return milliBase ? id / padding / 1000 : id / padding;
    }

    /**
     * Convert from id to {@link ZonedDateTime}.
     * 
     * @param id A target ID.
     * @return Epoch time.
     */
    public final ZonedDateTime computeDateTime(long id) {
        return Chrono.utcByMills(computeEpochTime(id) * (milliBase ? 1 : 1000));
    }

    /**
     * Convert from {@link ZonedDateTime} to id.
     * 
     * @param time A target date-time.
     * @return The computed ID.
     */
    public final long computeID(ZonedDateTime time) {
        long epoch = milliBase ? time.toInstant().toEpochMilli() : time.toEpochSecond();
        return epoch * padding;
    }

    /**
     * Convert from epoch time to id.
     * 
     * @param epochMilli A target date-time.
     * @return The computed ID.
     */
    public final long computeID(long epochMilli) {
        if (milliBase) {
            return epochMilli * padding;
        } else {
            return ((long) (epochMilli * 0.001)) * padding;
        }
    }

    /**
     * Create {@link Execution} with context from JSON.
     * 
     * @param side A side of execution.
     * @param size A size of execution.
     * @param price A price of execution.
     * @param epochMillis A time of execution.
     * @return
     */
    public final Execution createExecution(JSON json, String side, String size, String price, String epochMillis) {
        return createExecution(json, side, size, price, epochMillis, new long[3]);
    }

    /**
     * Create {@link Execution} with context from JSON.
     * 
     * @param side A side of execution.
     * @param size A size of execution.
     * @param price A price of execution.
     * @param epochMillis A time of execution.
     * @return
     */
    public final Execution createExecution(JSON json, String side, String size, String price, String epochMillis, long[] threeLength) {
        return createExecution(json.get(Direction.class, side), json.get(Num.class, size), json.get(Num.class, price), json
                .get(long.class, epochMillis), threeLength);
    }

    /**
     * Create {@link Execution} with context.
     * 
     * @param side A side of execution.
     * @param size A size of execution.
     * @param price A price of execution.
     * @param epochMillis A time of execution.
     * @return
     */
    public final Execution createExecution(Direction side, Num size, Num price, long epochMillis) {
        return createExecution(side, size, price, epochMillis, new long[3]);
    }

    /**
     * Create {@link Execution} with context.
     * 
     * @param side A side of execution.
     * @param size A size of execution.
     * @param price A price of execution.
     * @param date A time of execution.
     * @param threeLength The context data which must be 3 length long array.
     * @return
     */
    public final Execution createExecution(Direction side, Num size, Num price, ZonedDateTime date, long[] threeLength) {
        return createExecution(side, size, price, date.toInstant().toEpochMilli(), threeLength);
    }

    /**
     * Create {@link Execution} with context.
     * 
     * @param side A side of execution.
     * @param size A size of execution.
     * @param price A price of execution.
     * @param epochMillis A time of execution.
     * @param threeLength The context data which must be 3 length long array.
     * @return
     */
    public final Execution createExecution(Direction side, Num size, Num price, long epochMillis, long[] threeLength) {
        long sideType = side.ordinal();
        long id;
        int consecutive;
        long time = milliBase ? epochMillis : (long) (epochMillis * 0.001);

        if (time != threeLength[0]) {
            id = computeID(epochMillis);
            consecutive = Execution.ConsecutiveDifference;

            threeLength[0] = time;
            threeLength[1] = sideType;
            threeLength[2] = 0;
        } else {
            id = computeID(epochMillis) + ++threeLength[2];
            consecutive = sideType != threeLength[1] ? Execution.ConsecutiveDifference
                    : side == Direction.BUY ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;

            threeLength[0] = time;
            threeLength[1] = sideType;
        }

        return Execution.with.direction(side, size).id(id).price(price).date(Chrono.utcByMills(epochMillis)).consecutive(consecutive);
    }

    /**
     * Helper method to calculate consecutive type.
     * 
     * @param side
     * @param epochMillis
     * @param threeLength
     * @return
     */
    public static int computeConsecutive(Direction side, long epochMillis, long[] threeLength) {
        long sideType = side.ordinal();
        int consecutive;

        if (epochMillis != threeLength[0]) {
            consecutive = Execution.ConsecutiveDifference;

            threeLength[0] = epochMillis;
            threeLength[1] = sideType;
        } else {
            consecutive = sideType != threeLength[1] ? Execution.ConsecutiveDifference
                    : side == Direction.BUY ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;

            threeLength[0] = epochMillis;
            threeLength[1] = sideType;
        }
        return consecutive;
    }

    /**
     * Create pseudo {@link Execution}s from OHLCV candle.
     * 
     * @param open The open price.
     * @param high The highest price.
     * @param low The lowest price.
     * @param close The close price.
     * @param volume A spaned valume.
     * @param epochMillis A starting time.
     * @return
     */
    public final List<Execution> createExecutions(Num open, Num high, Num low, Num close, Num volume, long epochMillis, Span span) {
        if (volume.isZero()) {
            return Collections.EMPTY_LIST;
        }

        List<Execution> list = new ArrayList(4);

        boolean bull = open.isLessThan(close);
        Direction[] sides = bull ? new Direction[] {SELL, BUY, SELL, BUY} : new Direction[] {BUY, SELL, BUY, SELL};
        Num[] prices = bull ? new Num[] {open, low, high, close} : new Num[] {open, high, low, close};
        Num volume4 = volume.divide(4);

        for (int i = 0; i < prices.length; i++) {
            long millis = epochMillis + i * (span.duration.toMillis() / 4);
            long id = computeID(millis);

            list.add(Execution.with.direction(sides[i], volume4)
                    .price(prices[i])
                    .id(id)
                    .date(computeDateTime(id))
                    .consecutive(Execution.ConsecutivePseudoDifference));
        }
        return list;
    }
}