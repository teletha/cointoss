/*
 * Copyright (C) 2021 cointoss Development Team
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
import cointoss.util.arithmetic.Num;
import cointoss.util.feather.TemporalData;

public final class Tick implements TemporalData {

    /** The empty dummy. */
    static final Tick EMPTY = new Tick();

    /** Begin time of this tick (epoch second). */
    public final long openTime;

    /** Open price of the period */
    public final double openPrice;

    /** Close price of the period. */
    double closePrice;

    /** Max price of the period */
    double highPrice;

    /** Min price of the period */
    double lowPrice;

    /** Snapshot of long volume at tick initialization. */
    double longVolume;

    /** Snapshot of long losscut volume at tick initialization. */
    double longLosscutVolume;

    /** Snapshot of short volume at tick initialization. */
    double shortVolume;

    /** Snapshot of short losscut volume at tick initialization. */
    double shortLosscutVolume;

    /** The source ticker. */
    Ticker ticker;

    /**
     * Empty Dummt Tick.
     */
    private Tick() {
        this.openTime = 0;
        this.openPrice = closePrice = highPrice = lowPrice = 0;
    }

    /**
     * New {@link Tick}.
     * 
     * @param startEpochSeconds A start time of period.
     * @param open A open price.
     * @param ticker The data source.
     */
    Tick(long startEpochSeconds, Num open, Ticker ticker) {
        this(startEpochSeconds, open.doubleValue(), ticker);
    }

    /**
     * New {@link Tick}.
     * 
     * @param startEpochSeconds A start time of period.
     * @param open A open price.
     * @param ticker The data source.
     */
    Tick(long startEpochSeconds, double open, Ticker ticker) {
        this.openTime = startEpochSeconds;
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.ticker = ticker;
        this.longVolume = ticker.manager.longVolume;
        this.longLosscutVolume = ticker.manager.longLosscutVolume;
        this.shortVolume = ticker.manager.shortVolume;
        this.shortLosscutVolume = ticker.manager.shortLosscutVolume;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime date() {
        return Chrono.utcByMills(openTime * 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long seconds() {
        return openTime;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double openPrice() {
        return openPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double closePrice() {
        return ticker == null ? closePrice : ticker.manager.latest.v.price.doubleValue();
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double highPrice() {
        return highPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double lowPrice() {
        return lowPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double upperPrice() {
        double close = closePrice();
        return openPrice < close ? close : openPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double lowerPrice() {
        double close = closePrice();
        return openPrice < close ? openPrice : close;
    }

    /**
     * Heikin price (sometimes called the pivot point) refers to the arithmetic average of the high,
     * low, and closing prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public double heikinPrice() {
        return (highPrice + lowPrice + openPrice + closePrice()) / 4;
    }

    /**
     * Typical price (sometimes called the pivot point) refers to the arithmetic average of the
     * high, low, and closing prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public double typicalPrice() {
        return (highPrice + lowPrice + closePrice()) / 3;
    }

    /**
     * Median price (sometimes called the high-low price) refers to the arithmetic average of the
     * high and low prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public double medianPrice() {
        return (highPrice + lowPrice) / 2;
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
        return ticker == null ? longVolume : ticker.manager.longVolume - longVolume;
    }

    /**
     * Retrieve the tick related value.
     */
    public double longLosscutVolume() {
        return ticker == null ? longLosscutVolume : ticker.manager.longLosscutVolume - longLosscutVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double shortVolume() {
        return ticker == null ? shortVolume : ticker.manager.shortVolume - shortVolume;
    }

    /**
     * Retrieve the tick related value.
     */
    public double shortLosscutVolume() {
        return ticker == null ? shortLosscutVolume : ticker.manager.shortLosscutVolume - shortLosscutVolume;
    }

    /**
     * Compute the spread in prices.
     * 
     * @return
     */
    public double spread() {
        return highPrice() - lowPrice();
    }

    /**
     * Make this {@link Tick}'s related values fixed.
     */
    synchronized void freeze() {
        if (ticker != null) {
            ticker.spreadStats.add(spread());
            ticker.buyVolumeStats.add(longVolume());
            ticker.sellVolumeStats.add(shortVolume());
            ticker.typicalStats.add(typicalPrice());

            closePrice = closePrice();
            longVolume = longVolume();
            longLosscutVolume = longLosscutVolume();
            shortVolume = shortVolume();
            shortLosscutVolume = shortLosscutVolume();
            ticker = null;
        }
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBull() {
        return openPrice < closePrice();
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBear() {
        return openPrice > closePrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TICK ").append(" ")
                .append(Chrono.format(date()))
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