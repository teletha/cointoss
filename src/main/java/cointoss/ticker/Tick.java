/*
 * Copyright (C) 2020 cointoss Development Team
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

public final class Tick {

    /** The empty dummy. */
    static final Tick EMPTY = new Tick();

    /** Begin time of this tick (epoch second). */
    public final long openTime;

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
    double longVolume;

    /** Snapshot of long losscut volume at tick initialization. */
    double longLosscutVolume;

    /** Snapshot of long price increase at tick initialization. */
    double longPriceIncrease;

    /** Snapshot of short volume at tick initialization. */
    double shortVolume;

    /** Snapshot of short losscut volume at tick initialization. */
    double shortLosscutVolume;

    /** Snapshot of short price decrease at tick initialization. */
    double shortPriceDecrease;

    /**
     * Empty Dummt Tick.
     */
    private Tick() {
        this.openTime = 0;
        this.openPrice = closePrice = highPrice = lowPrice = Num.ZERO;
    }

    /**
     * New {@link Tick}.
     * 
     * @param openTime A start time of period.
     * @param open A open price.
     * @param realtime The realtime execution statistic.
     */
    Tick(long startEpochSeconds, Num open, TickerManager realtime) {
        this.openTime = startEpochSeconds;
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.realtime = realtime;
        this.longVolume = realtime.longVolume;
        this.longPriceIncrease = realtime.longPriceIncrease;
        this.longLosscutVolume = realtime.longLosscutVolume;
        this.shortVolume = realtime.shortVolume;
        this.shortPriceDecrease = realtime.shortPriceDecrease;
        this.shortLosscutVolume = realtime.shortLosscutVolume;
    }

    /**
     * Retrieve the start time of this {@link Tick}.
     * 
     * @return The start time.
     */
    public ZonedDateTime openTime() {
        return Chrono.utcByMills(openTime * 1000);
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
     * Typical price (sometimes called the pivot point) refers to the arithmetic average of the
     * high, low, and closing prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public Num typicalPrice() {
        return highPrice.plus(lowPrice).plus(closePrice()).divide(Num.THREE);
    }

    /**
     * Median price (sometimes called the high-low price) refers to the arithmetic average of the
     * high and low prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public Num medianPrice() {
        return highPrice.plus(lowPrice).divide(Num.TWO);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double volume() {
        return longVolume() + shortVolume();
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double longVolume() {
        return realtime == null ? longVolume : realtime.longVolume - longVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double longPriceIncrease() {
        return realtime == null ? longPriceIncrease : realtime.longPriceIncrease - longPriceIncrease;
    }

    /**
     * Retrieve the tick related value.
     */
    public double longLosscutVolume() {
        return realtime == null ? longLosscutVolume : realtime.longLosscutVolume - longLosscutVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double shortVolume() {
        return realtime == null ? shortVolume : realtime.shortVolume - shortVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double shortPriceDecrease() {
        return realtime == null ? shortPriceDecrease : realtime.shortPriceDecrease - shortPriceDecrease;
    }

    /**
     * Retrieve the tick related value.
     */
    public double shortLosscutVolume() {
        return realtime == null ? shortLosscutVolume : realtime.shortLosscutVolume - shortLosscutVolume;
    }

    /**
     * @return
     */
    public double priceVolatility() {
        double upPotencial = longVolume() == 0d ? 0 : longPriceIncrease() / longVolume();
        double downPotencial = shortVolume() == 0d ? 0 : shortPriceDecrease() / shortVolume();
        return upPotencial / downPotencial;
    }

    public double upRatio() {
        return longVolume() == 0d ? 0 : longPriceIncrease() * longVolume();
    }

    public double downRatio() {
        return shortVolume() == 0d ? 0 : shortPriceDecrease() * shortVolume();
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
        longLosscutVolume = longLosscutVolume();
        shortVolume = shortVolume();
        shortPriceDecrease = shortPriceDecrease();
        shortLosscutVolume = shortLosscutVolume();
        realtime = null;
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBull() {
        return openPrice.isLessThan(closePrice());
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBear() {
        return openPrice.isGreaterThan(closePrice());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TICK ").append(" ")
                .append(Chrono.format(openTime()))
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.hashCode(openTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tick == false) {
            return false;
        }

        Tick other = (Tick) obj;
        return openTime == other.openTime;
    }
}