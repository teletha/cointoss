/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import static cointoss.Direction.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.JSON;

/**
 * This {@link MarketService} is useful to use in the following cases
 * <ul>
 * <li>The execution ID obtained by the REST API and the Websocket API are not the same.</li>
 * <li>The REST or Websocket API or both do not contain the execution ID.</li>
 * <li>REST API cannot obtain the necessary execution information and CandleStick is used
 * instead.</li>
 * </ul>
 */
public abstract class TimestampBasedMarketService extends MarketService {

    /** The padding to avoid equating multiple trades occurring at the same time. */
    protected final long padding;

    /**
     * @param exchange
     * @param marketName
     * @param setting
     */
    protected TimestampBasedMarketService(Exchange exchange, String marketName, MarketSetting setting, long padding) {
        super(exchange, marketName, setting);

        this.padding = padding;
    }

    /**
     * Convert from ID to epoch millis.
     * 
     * @param id A target ID.
     * @return Epoch time.
     */
    protected final long computeMilli(long id) {
        return id / padding;
    }

    /**
     * Convert from ID to epoch millis.
     * 
     * @param id A target ID.
     * @return Epoch time.
     */
    protected final long computeSec(long id) {
        return id / (padding * 1000);
    }

    /**
     * Convert from id to {@link ZonedDateTime}.
     * 
     * @param id A target ID.
     * @return Epoch time.
     */
    protected final ZonedDateTime computeDateTime(long id) {
        return Chrono.utcByMills(computeMilli(id));
    }

    /**
     * Convert from {@link ZonedDateTime} to ID.
     * 
     * @param date A target date-time.
     * @return The computed ID.
     */
    protected final long computeID(ZonedDateTime date) {
        return computeID(date.toInstant().toEpochMilli());
    }

    /**
     * Convert from {@link ZonedDateTime} to ID.
     * 
     * @param date A target date-time.
     * @return The computed ID.
     */
    protected final long computeID(long epochMillis) {
        return epochMillis * padding;
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
    protected final Execution createExecution(JSON json, String side, String size, String price, String epochMillis) {
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
    protected final Execution createExecution(JSON json, String side, String size, String price, String epochMillis, long[] threeLength) {
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
    protected final Execution createExecution(Direction side, Num size, Num price, long epochMillis) {
        return createExecution(side, size, price, epochMillis, new long[3]);
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
    protected final Execution createExecution(Direction side, Num size, Num price, long epochMillis, long[] threeLength) {
        long sideType = side.ordinal();
        long id;
        int consecutive;

        if (epochMillis != threeLength[0]) {
            id = computeID(epochMillis);
            consecutive = Execution.ConsecutiveDifference;

            threeLength[0] = epochMillis;
            threeLength[1] = sideType;
            threeLength[2] = 0;
        } else {
            id = computeID(epochMillis) + threeLength[2]++;
            consecutive = sideType != threeLength[1] ? Execution.ConsecutiveDifference
                    : side == Direction.BUY ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;

            threeLength[0] = epochMillis;
            threeLength[1] = sideType;
        }

        return Execution.with.direction(side, size).id(id).price(price).date(Chrono.utcByMills(epochMillis)).consecutive(consecutive);
    }

    /**
     * Create pseudo {@link Execution}s from OHLCV candle.
     * 
     * @param open
     * @param high
     * @param low
     * @param close
     * @param volume
     * @param epochMillis
     * @return
     */
    protected final List<Execution> createExecutions(Num open, Num high, Num low, Num close, Num volume, long epochMillis) {
        if (volume.isZero()) {
            return Collections.EMPTY_LIST;
        }

        List<Execution> list = new ArrayList(4);

        boolean bull = open.isLessThan(close);
        Direction[] sides = bull ? new Direction[] {SELL, BUY, SELL, BUY} : new Direction[] {BUY, SELL, BUY, SELL};
        Num[] prices = bull ? new Num[] {open, low, high, close} : new Num[] {open, high, low, close};
        Num volume4 = volume.divide(4);

        for (int i = 0; i < prices.length; i++) {
            long millis = epochMillis + i * 15000;
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
