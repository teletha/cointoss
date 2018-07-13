/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.ZonedDateTime;

import cointoss.util.Num;

/**
 * @version 2018/07/14 0:29:54
 */
public final class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** Close price of the period. */
    Num closePrice;

    /** Max price of the period */
    Num highPrice;

    /** Min price of the period */
    Num lowPrice;

    /** The realtime execution statistic. */
    TickerManager realtime;

    /** Snapshot of long volume at tick initialization. */
    Num longVolume;

    /** Snapshot of long price increase at tick initialization. */
    Num longPriceIncrease;

    /** Snapshot of short volume at tick initialization. */
    Num shortVolume;

    /** Snapshot of short price decrease at tick initialization. */
    Num shortPriceDecrease;

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param span A tick span.
     * @param open A open price.
     * @param realtime The realtime execution statistic.
     */
    Tick(ZonedDateTime start, TickSpan span, Num open, TickerManager realtime) {
        this.start = start;
        this.end = start.plus(span.duration);
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.realtime = realtime;
        this.longVolume = realtime.longVolume;
        this.longPriceIncrease = realtime.longPriceIncrease;
        this.shortVolume = realtime.shortVolume;
        this.shortPriceDecrease = realtime.shortPriceDecrease;
    }

    /**
     * Retrieve the start time of this {@link Tick}.
     * 
     * @return The start time.
     */
    public ZonedDateTime start() {
        return start;
    }

    /**
     * Retrieve the end time of this {@link Tick}.
     * 
     * @return The end time.
     */
    public ZonedDateTime end() {
        return end;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num openPrice() {
        return openPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num closePrice() {
        return realtime == null ? closePrice : realtime.latest.v.price;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num highPrice() {
        return highPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num lowPrice() {
        return lowPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num volume() {
        return longVolume.plus(shortVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num longVolume() {
        return realtime == null ? longVolume : realtime.longVolume.minus(longVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num longPriceIncrease() {
        return realtime == null ? longPriceIncrease : realtime.longPriceIncrease.minus(longPriceIncrease);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num shortVolume() {
        return realtime == null ? shortVolume : realtime.shortVolume.minus(shortVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num shortPriceDecrease() {
        return realtime == null ? shortPriceDecrease : realtime.shortPriceDecrease.minus(shortPriceDecrease);
    }

    /**
     * @return
     */
    public Num priceVolatility() {
        Num upPotencial = longVolume().isZero() ? Num.ZERO : longPriceIncrease().divide(longVolume());
        Num downPotencial = shortVolume().isZero() ? Num.ZERO : shortPriceDecrease().divide(shortVolume());
        return upPotencial.divide(downPotencial).scale(2);
    }

    public Num upRatio() {
        return longVolume().isZero() ? Num.ZERO : longPriceIncrease().multiply(longVolume());
    }

    public Num downRatio() {
        return shortVolume().isZero() ? Num.ZERO : shortPriceDecrease().multiply(shortVolume());
    }

    /**
     * Make this {@link Tick}'s related values fixed.
     * 
     * @return
     */
    void freeze() {
        closePrice = closePrice();
        longVolume = longVolume();
        longPriceIncrease = longPriceIncrease();
        shortVolume = shortVolume();
        shortPriceDecrease = shortPriceDecrease();
        realtime = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TICK ");
        builder.append(start)
                .append(" ")
                .append(end)
                .append(" ")
                .append(openPrice)
                .append(" ")
                .append(closePrice())
                .append(" ")
                .append(highPrice)
                .append(" ")
                .append(lowPrice)
                .append(" ")
                .append(longVolume())
                .append(" ")
                .append(shortVolume());

        return builder.toString();
    }
}
