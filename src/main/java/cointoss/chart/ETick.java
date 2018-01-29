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

import java.time.ZonedDateTime;

import cointoss.Execution;
import cointoss.util.Num;

/**
 * @version 2018/01/29 10:32:49
 */
public class ETick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** Close price of the period */
    public Num closePrice = Num.ZERO;

    /** Max price of the period */
    public Num highPrice = Num.ZERO;

    /** Min price of the period */
    public Num lowPrice = Num.MAX;

    /** Volume of the period */
    public Num volume = Num.ZERO;

    /** Traded amount during the period */
    public Num amount = Num.ZERO;

    /** Traded amount during the period */
    public Num amountSquare = Num.ZERO;

    /** Volume of the period */
    public Num longVolume = Num.ZERO;

    /** Volume of the period */
    public Num longPriceIncrese = Num.ZERO;

    /** Volume of the period */
    public Num shortVolume = Num.ZERO;

    /** Volume of the period */
    public Num shortPriceDecrease = Num.ZERO;

    /**
     * @param startTime
     * @param endTime
     */
    public ETick(ZonedDateTime startTime, ZonedDateTime endTime, Num open) {
        this.start = startTime;
        this.end = endTime;
        this.openPrice = open;
    }

    /**
     * Update date.
     * 
     * @param exe
     */
    public void update(Execution exe) {
        Num latest = closePrice == null ? openPrice : closePrice;
        closePrice = exe.price;
        highPrice = Num.max(highPrice, exe.price);
        lowPrice = Num.min(lowPrice, exe.price);
        volume = volume.plus(exe.size);
        amount = amount.plus(exe.price.multiply(exe.size));

        if (exe.side.isBuy()) {
            longVolume = longVolume.plus(exe.size);
            longPriceIncrese = longPriceIncrese.plus(exe.price.minus(latest));
        } else {
            shortVolume = shortVolume.plus(exe.size);
            shortPriceDecrease = shortPriceDecrease.plus(latest.minus(exe.price));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(start)
                .append(" ")
                .append(end)
                .append(" ")
                .append(openPrice)
                .append(" ")
                .append(closePrice)
                .append(" ")
                .append(highPrice)
                .append(" ")
                .append(lowPrice)
                .append(" ")
                .append(volume)
                .append(" ")
                .append(amount);

        return builder.toString();
    }
}
