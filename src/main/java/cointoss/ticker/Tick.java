/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.lang.ref.WeakReference;
import java.time.ZonedDateTime;

import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.Signal;

public final class Tick {

    /** The empty dummy. */
    static final Tick EMPTY = new Tick();

    /** Begin time of the tick (epoch second). */
    public final long startSeconds;

    /** End time of the tick (epoch second). */
    public final long endSeconds;

    /** Open id of the period. */
    public final long openId;

    /** Sampled network delay. */
    public final int delay;

    /** Open price of the period */
    public final Num openPrice;

    /** The previous tick. */
    private WeakReference<Tick> previous;

    /** Close price of the period. */
    Num closePrice;

    /** Max price of the period */
    Num highPrice;

    /** Min price of the period */
    Num lowPrice;

    /** The realtime execution statistic. */
    TickerManager realtime;

    /** Snapshot of long count at tick initialization. */
    int longCount;

    /** Snapshot of losscut long count at tick initialization. */
    int longLosscutCount;

    /** Snapshot of long volume at tick initialization. */
    double longVolume;

    /** Snapshot of long price increase at tick initialization. */
    double longPriceIncrease;

    /** Snapshot of short count at tick initialization. */
    int shortCount;

    /** Snapshot of losscut short count at tick initialization. */
    int shortLosscutCount;

    /** Snapshot of short volume at tick initialization. */
    double shortVolume;

    /** Snapshot of short price decrease at tick initialization. */
    double shortPriceDecrease;

    /**
     * Empty Dummt Tick.
     */
    private Tick() {
        this.startSeconds = 0;
        this.endSeconds = 0;
        this.delay = 0;
        this.openId = 0;
        this.openPrice = closePrice = highPrice = lowPrice = Num.ZERO;
    }

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param span A tick span.
     * @param id A open id.
     * @param open A open price.
     * @param realtime The realtime execution statistic.
     */
    Tick(Tick previous, long startEpochSeconds, Span span, long id, int delay, Num open, TickerManager realtime) {
        this.previous = new WeakReference(previous);
        this.startSeconds = startEpochSeconds;
        this.endSeconds = startEpochSeconds + span.seconds;
        this.openId = id;
        this.delay = delay;
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.realtime = realtime;
        this.longCount = realtime.longCount;
        this.longVolume = realtime.longVolume;
        this.longPriceIncrease = realtime.longPriceIncrease;
        this.longLosscutCount = realtime.longLosscutCount;
        this.shortCount = realtime.shortCount;
        this.shortVolume = realtime.shortVolume;
        this.shortPriceDecrease = realtime.shortPriceDecrease;
        this.shortLosscutCount = realtime.shortLosscutCount;
    }

    /**
     * Get the previous {@link Tick}.
     * 
     * @return
     */
    public Tick previous() {
        return previous.get();
    }

    /**
     * Retrieve the start time of this {@link Tick}.
     * 
     * @return The start time.
     */
    public ZonedDateTime start() {
        return Chrono.utcByMills(startSeconds * 1000);
    }

    /**
     * Retrieve the end time of this {@link Tick}.
     * 
     * @return The end time.
     */
    public ZonedDateTime end() {
        return Chrono.utcByMills(endSeconds * 1000);
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
     */
    public int longCount() {
        return realtime == null ? longCount : realtime.longCount - longCount;
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
    public int longLosscutCount() {
        return realtime == null ? longLosscutCount : realtime.longLosscutCount - longLosscutCount;
    }

    /**
     * Retrieve the tick related value.
     */
    public int shortCount() {
        return realtime == null ? shortCount : realtime.shortCount - shortCount;
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
    public int shortLosscutCount() {
        return realtime == null ? shortLosscutCount : realtime.shortLosscutCount - shortLosscutCount;
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
        longCount = longCount();
        longVolume = longVolume();
        longPriceIncrease = longPriceIncrease();
        longLosscutCount = longLosscutCount();
        shortCount = shortCount();
        shortVolume = shortVolume();
        shortPriceDecrease = shortPriceDecrease();
        shortLosscutCount = shortLosscutCount();
        realtime = null;
    }

    /**
     * Retrieve the previous tick sequentially.
     * 
     * @param size A number of ticks.
     */
    public Signal<Tick> previous(int size) {
        return new Signal<>((observer, disposer) -> {
            int count = 0;
            Tick now = this;
            while (count < size && now != null && disposer.isNotDisposed()) {
                observer.accept(now);

                now = now.previous();
                count++;
            }
            return disposer;
        });
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
                .append(Chrono.format(start()))
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
        return Long.hashCode(startSeconds);
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
        return startSeconds == other.startSeconds;
    }
}
