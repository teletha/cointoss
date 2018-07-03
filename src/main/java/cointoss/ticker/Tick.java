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
 * @version 2018/07/03 9:46:26
 */
public class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** Max price of the period */
    Num highPrice = Num.ZERO;

    /** Min price of the period */
    Num lowPrice = Num.MAX;

    BaseStatistics base;

    BaseStatistics snapshot;

    private Tick(ZonedDateTime time) {
        this.start = time;
        this.end = time;
        this.openPrice = this.highPrice = this.lowPrice = Num.ZERO;
        this.snapshot = this.base = new BaseStatistics();
    }

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param end A end time of period.
     * @param open A open price.
     */
    public Tick(ZonedDateTime start, ZonedDateTime end, Num open) {
        this.start = start;
        this.end = end;
        this.openPrice = this.highPrice = this.lowPrice = open;
    }

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param end A end time of period.
     * @param open A open price.
     */
    public Tick(ZonedDateTime start, ZonedDateTime end, BaseStatistics snapshot) {
        this(start, end, snapshot.latestPrice);
        this.snapshot = snapshot;
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
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num openPrice() {
        return openPrice;
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num closePrice() {
        return base == null ? snapshot.latestPrice : base.latestPrice;
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num highPrice() {
        return highPrice;
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num lowPrice() {
        return lowPrice;
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num longVolume() {
        return base == null ? snapshot.longVolume : base.longVolume.minus(snapshot.longVolume);
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num longPriceIncrease() {
        return base == null ? snapshot.longPriceIncrease : base.longPriceIncrease.minus(snapshot.longPriceIncrease);
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num shortVolume() {
        return base == null ? snapshot.shortVolume : base.shortVolume.minus(snapshot.shortVolume);
    }

    /**
     * Retieve the tick related value.
     * 
     * @return The tick related value.
     */
    public final Num shortPriceDecrease() {
        return base == null ? snapshot.shortPriceDecrease : base.shortPriceDecrease.minus(snapshot.shortPriceDecrease);
    }

    /**
     * @return
     */
    public final Num priceVolatility() {
        Num upPotencial = longVolume().isZero() ? Num.ZERO : longPriceIncrease().divide(longVolume());
        Num downPotencial = shortVolume().isZero() ? Num.ZERO : shortPriceDecrease().divide(shortVolume());
        return upPotencial.divide(downPotencial).scale(2);
    }

    public final Num upRatio() {
        return longVolume().isZero() ? Num.ZERO : longPriceIncrease().multiply(longVolume());
    }

    public final Num downRatio() {
        return shortVolume().isZero() ? Num.ZERO : shortPriceDecrease().multiply(shortVolume());
    }

    /**
     * Make this {@link Tick} immutable.
     * 
     * @return
     */
    void freeze() {
        if (base != null) {
            BaseStatistics snapshot = new BaseStatistics();
            snapshot.latestPrice = base.latestPrice;
            snapshot.longVolume = longVolume();
            snapshot.longPriceIncrease = longPriceIncrease();
            snapshot.shortVolume = shortVolume();
            snapshot.shortPriceDecrease = shortPriceDecrease();

            this.snapshot = snapshot;
            this.base = null;
        }
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
                .append(shortVolume())
                .append(" ")
                .append(base)
                .append(" ")
                .append(snapshot);

        return builder.toString();
    }

    public static Tick initial() {
        return new Tick(Chrono.utc(1970, 1, 1));
    }
}
