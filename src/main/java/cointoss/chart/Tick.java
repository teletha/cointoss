/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import java.time.Duration;
import java.time.ZonedDateTime;

import cointoss.Execution;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 8:40:27
 */
public class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime beginTime;

    /** End time of the tick */
    public final ZonedDateTime endTime;

    /** Open price of the period */
    public final Decimal openPrice;

    /** Close price of the period */
    public Decimal closePrice = null;

    /** Max price of the period */
    public Decimal maxPrice = Decimal.ZERO;

    /** Min price of the period */
    public Decimal minPrice = Decimal.MAX;

    /** Traded amount during the period */
    public Decimal amount = Decimal.ZERO;

    /** Volume of the period */
    public Decimal volume = Decimal.ZERO;

    /** Trade count */
    protected int trades = 0;

    /**
     * Decode.
     * 
     * @param value
     */
    Tick(String value) {
        String[] values = value.split(" ");

        beginTime = ZonedDateTime.parse(values[0]);
        endTime = ZonedDateTime.parse(values[1]);
        openPrice = Decimal.valueOf(values[2]);
        closePrice = Decimal.valueOf(values[3]);
        maxPrice = Decimal.valueOf(values[4]);
        minPrice = Decimal.valueOf(values[5]);
        volume = Decimal.valueOf(values[6]);
    }

    /**
    * 
    */
    Tick(Execution exe, Duration duration) {
        beginTime = exe.exec_date.withSecond(0).withNano(0);
        endTime = beginTime.plus(duration);
        openPrice = exe.price;
    }

    /**
    * 
    */
    Tick(Tick exe, Duration duration) {
        beginTime = exe.beginTime;
        endTime = beginTime.plus(duration);
        openPrice = exe.openPrice;
    }

    /**
     * Assign date.
     * 
     * @param exe
     */
    void tick(Execution exe) {
        closePrice = exe.price;
        maxPrice = maxPrice.max(exe.price);
        minPrice = minPrice.min(exe.price);
        volume = volume.plus(exe.size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(beginTime)
                .append(" ")
                .append(endTime)
                .append(" ")
                .append(openPrice)
                .append(" ")
                .append(closePrice)
                .append(" ")
                .append(maxPrice)
                .append(" ")
                .append(minPrice)
                .append(" ")
                .append(volume);

        return builder.toString();
    }
}
