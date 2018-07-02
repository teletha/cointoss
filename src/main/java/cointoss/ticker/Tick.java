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

import cointoss.util.Chrono;
import cointoss.util.Num;

/**
 * @version 2018/07/03 8:40:50
 */
public class Tick {

    /** The null object. */
    public static final Tick PAST = new Tick(Chrono.utc(2000, 1, 1), Chrono.utc(2000, 1, 1), Num.ZERO);

    /** The null object. */
    public static final Tick NOW = new Tick(Chrono.utcNow(), Chrono.utcNow(), Num.ZERO);

    static {
        PAST.closePrice = Num.ZERO;
        NOW.closePrice = Num.ZERO;
    }

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** Close price of the period */
    private Num closePrice = null;

    /** Max price of the period */
    public Num highPrice = Num.ZERO;

    /** Min price of the period */
    public Num lowPrice = Num.MAX;

    /** Volume of the period */
    public Num longVolume = Num.ZERO;

    /** Volume of the period */
    public Num longPriceIncrese = Num.ZERO;

    /** Volume of the period */
    public Num shortVolume = Num.ZERO;

    /** Volume of the period */
    public Num shortPriceDecrease = Num.ZERO;

    BaseStatistics base;

    BaseStatistics snapshot;

    /**
    * 
    */
    public Tick(ZonedDateTime start, ZonedDateTime end, Num open) {
        this.start = start;
        this.end = end;
        this.openPrice = this.closePrice = this.highPrice = this.lowPrice = open;
    }

    /**
     * Get the beginTime property of this {@link Tick}.
     * 
     * @return The beginTime property.
     */
    public final ZonedDateTime start() {
        return start;
    }

    /**
     * Get the endTime property of this {@link Tick}.
     * 
     * @return The endTime property.
     */
    public final ZonedDateTime end() {
        return end;
    }

    /**
     * Get the openPrice property of this {@link Tick}.
     * 
     * @return The openPrice property.
     */
    public final Num getOpenPrice() {
        return openPrice;
    }

    /**
     * Get the volume property of this {@link Tick}.
     * 
     * @return The volume property.
     */
    public final Num getVolume() {
        return longVolume.plus(shortVolume);
    }

    /**
     * Get the closePrice property of this {@link Tick}.
     * 
     * @return The closePrice property.
     */
    public Num closePrice() {
        return base == null ? snapshot.latestPrice : base.latestPrice;
    }

    public final Num longVolume() {
        return base == null ? snapshot.longVolume : base.longVolume.minus(snapshot.longVolume);
    }

    public final Num shortVolume() {
        return base == null ? snapshot.shortVolume : base.shortVolume.minus(snapshot.shortVolume);
    }

    /**
     * @return
     */
    public final Num priceVolatility() {
        Num upPotencial = longVolume.isZero() ? Num.ZERO : longPriceIncrese.divide(longVolume);
        Num downPotencial = shortVolume.isZero() ? Num.ZERO : shortPriceDecrease.divide(shortVolume);
        return upPotencial.divide(downPotencial).scale(2);
    }

    public final Num upRatio() {
        return longVolume.isZero() ? Num.ZERO : longPriceIncrese.multiply(longVolume);
    }

    public final Num downRatio() {
        return shortVolume.isZero() ? Num.ZERO : shortPriceDecrease.multiply(shortVolume);
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
                .append(closePrice)
                .append(" ")
                .append(highPrice)
                .append(" ")
                .append(lowPrice)
                .append(" ")
                .append(longVolume)
                .append(" ")
                .append(shortVolume)
                .append(" ")
                .append(base)
                .append(" ")
                .append(snapshot);

        return builder.toString();
    }
}
