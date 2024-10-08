/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import cointoss.util.feather.Timelinable;
import hypatia.Num;
import typewriter.api.model.IdentifiableModel;

@SuppressWarnings("unused")
public final class Tick extends IdentifiableModel implements Timelinable {

    /** Begin time of this tick (epoch second). */
    long openTime;

    /** Open price of the period */
    double openPrice;

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
     * Empty Tick.
     */
    Tick() {
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
    public long getId() {
        return openTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setId(long id) {
        openTime = id;
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
    public long openTime() {
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
     * Get the openPrice property of this {@link Tick}.
     * 
     * @return The openPrice property.
     */
    private double getOpenPrice() {
        return openPrice;
    }

    /**
     * Set the openPrice property of this {@link Tick}.
     * 
     * @param openPrice The openPrice value to set.
     */
    private void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    /**
     * Get the closePrice property of this {@link Tick}.
     * 
     * @return The closePrice property.
     */
    private double getClosePrice() {
        return closePrice;
    }

    /**
     * Set the closePrice property of this {@link Tick}.
     * 
     * @param closePrice The closePrice value to set.
     */
    private void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(closePrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(highPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lowPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(openPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (openTime ^ (openTime >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tick)) return false;
        Tick other = (Tick) obj;
        if (Double.doubleToLongBits(closePrice) != Double.doubleToLongBits(other.closePrice)) return false;
        if (Double.doubleToLongBits(highPrice) != Double.doubleToLongBits(other.highPrice)) return false;
        if (Double.doubleToLongBits(lowPrice) != Double.doubleToLongBits(other.lowPrice)) return false;
        if (Double.doubleToLongBits(openPrice) != Double.doubleToLongBits(other.openPrice)) return false;
        if (openTime != other.openTime) return false;
        return true;
    }

    /**
     * Get the highPrice property of this {@link Tick}.
     * 
     * @return The highPrice property.
     */
    private double getHighPrice() {
        return highPrice;
    }

    /**
     * Set the highPrice property of this {@link Tick}.
     * 
     * @param highPrice The highPrice value to set.
     */
    private void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    /**
     * Get the lowPrice property of this {@link Tick}.
     * 
     * @return The lowPrice property.
     */
    private double getLowPrice() {
        return lowPrice;
    }

    /**
     * Set the lowPrice property of this {@link Tick}.
     * 
     * @param lowPrice The lowPrice value to set.
     */
    private void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    /**
     * Get the longVolume property of this {@link Tick}.
     * 
     * @return The longVolume property.
     */
    private double getLongVolume() {
        return longVolume;
    }

    /**
     * Set the longVolume property of this {@link Tick}.
     * 
     * @param longVolume The longVolume value to set.
     */
    private void setLongVolume(double longVolume) {
        this.longVolume = longVolume;
    }

    /**
     * Get the longLosscutVolume property of this {@link Tick}.
     * 
     * @return The longLosscutVolume property.
     */
    private double getLongLosscutVolume() {
        return longLosscutVolume;
    }

    /**
     * Set the longLosscutVolume property of this {@link Tick}.
     * 
     * @param longLosscutVolume The longLosscutVolume value to set.
     */
    private void setLongLosscutVolume(double longLosscutVolume) {
        this.longLosscutVolume = longLosscutVolume;
    }

    /**
     * Get the shortVolume property of this {@link Tick}.
     * 
     * @return The shortVolume property.
     */
    private double getShortVolume() {
        return shortVolume;
    }

    /**
     * Set the shortVolume property of this {@link Tick}.
     * 
     * @param shortVolume The shortVolume value to set.
     */
    private void setShortVolume(double shortVolume) {
        this.shortVolume = shortVolume;
    }

    /**
     * Get the shortLosscutVolume property of this {@link Tick}.
     * 
     * @return The shortLosscutVolume property.
     */
    private double getShortLosscutVolume() {
        return shortLosscutVolume;
    }

    /**
     * Set the shortLosscutVolume property of this {@link Tick}.
     * 
     * @param shortLosscutVolume The shortLosscutVolume value to set.
     */
    private void setShortLosscutVolume(double shortLosscutVolume) {
        this.shortLosscutVolume = shortLosscutVolume;
    }
}