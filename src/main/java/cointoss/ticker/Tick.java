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
 * @version 2018/07/05 2:00:28
 */
public final class Tick {

    /** Begin time of the tick */
    public ZonedDateTime start;

    /** End time of the tick */
    public ZonedDateTime end;

    /** Open price of the period */
    public Num openPrice;

    /** Max price of the period */
    Num highPrice;

    /** Min price of the period */
    Num lowPrice;

    /** The realtime {@link Totality}. */
    Totality realtime;

    /** The snapshot {@link Totality} for the period. */
    Totality snapshot;

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param end A end time of period.
     * @param open A open price.
     */
    Tick(ZonedDateTime start, TickSpan span, Num open, Totality realtime) {
        this.start = start;
        this.end = start.plus(span.duration);
        this.openPrice = this.highPrice = this.lowPrice = open;
        this.realtime = realtime;
        this.snapshot = realtime.snapshot();
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
        return realtime == null ? snapshot.latestPrice : realtime.latestPrice;
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
    public Num longVolume() {
        return realtime == null ? snapshot.longVolume : realtime.longVolume.minus(snapshot.longVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num longPriceIncrease() {
        return realtime == null ? snapshot.longPriceIncrease : realtime.longPriceIncrease.minus(snapshot.longPriceIncrease);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num shortVolume() {
        return realtime == null ? snapshot.shortVolume : realtime.shortVolume.minus(snapshot.shortVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num shortPriceDecrease() {
        return realtime == null ? snapshot.shortPriceDecrease : realtime.shortPriceDecrease.minus(snapshot.shortPriceDecrease);
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
        snapshot.latestPrice = realtime.latestPrice;
        snapshot.longVolume = longVolume();
        snapshot.longPriceIncrease = longPriceIncrease();
        snapshot.shortVolume = shortVolume();
        snapshot.shortPriceDecrease = shortPriceDecrease();
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
                .append(shortVolume())
                .append(" ")
                .append(realtime)
                .append(" ")
                .append(snapshot);

        return builder.toString();
    }
}
