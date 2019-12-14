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

import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

public final class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** Begin time of the tick */
    public final long startSeconds;

    /** End time of the tick */
    public final ZonedDateTime end;

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

    /** Snapshot of long volume at tick initialization. */
    double buyVolume;

    /** Snapshot of long price increase at tick initialization. */
    double buyPriceIncrease;

    /** Snapshot of short volume at tick initialization. */
    double sellVolume;

    /** Snapshot of short price decrease at tick initialization. */
    double sellPriceDecrease;

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param span A tick span.
     * @param id A open id.
     * @param open A open price.
     * @param realtime The realtime execution statistic.
     */
    Tick(Tick previous, ZonedDateTime start, TimeSpan span, long id, int delay, Num open, TickerManager realtime) {
        this.previous = new WeakReference(previous);
        this.start = start;
        this.startSeconds = start.toEpochSecond();
        this.end = start.plus(span.duration);
        this.openId = id;
        this.delay = delay;
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.realtime = realtime;
        this.buyVolume = realtime.longVolume;
        this.buyPriceIncrease = realtime.longPriceIncrease;
        this.sellVolume = realtime.shortVolume;
        this.sellPriceDecrease = realtime.shortPriceDecrease;
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
        return buyVolume() + sellVolume();
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double buyVolume() {
        return realtime == null ? buyVolume : realtime.longVolume - buyVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double buyPriceIncrease() {
        return realtime == null ? buyPriceIncrease : realtime.longPriceIncrease - buyPriceIncrease;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double sellVolume() {
        return realtime == null ? sellVolume : realtime.shortVolume - sellVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double sellPriceDecrease() {
        return realtime == null ? sellPriceDecrease : realtime.shortPriceDecrease - sellPriceDecrease;
    }

    /**
     * @return
     */
    public double priceVolatility() {
        double upPotencial = buyVolume() == 0d ? 0 : buyPriceIncrease() / buyVolume();
        double downPotencial = sellVolume() == 0d ? 0 : sellPriceDecrease() / sellVolume();
        return upPotencial / downPotencial;
    }

    public double upRatio() {
        return buyVolume() == 0d ? 0 : buyPriceIncrease() * buyVolume();
    }

    public double downRatio() {
        return sellVolume() == 0d ? 0 : sellPriceDecrease() * sellVolume();
    }

    /**
     * Make this {@link Tick}'s related values fixed.
     * 
     * @return
     */
    void freeze() {
        closePrice = closePrice();
        buyVolume = buyVolume();
        buyPriceIncrease = buyPriceIncrease();
        sellVolume = sellVolume();
        sellPriceDecrease = sellPriceDecrease();
        realtime = null;
    }

    /**
     * Retrieve the previous tick sequentially.
     * 
     * @param size A number of ticks.
     */
    public Signal<Tick> previous(int size) {
        return I.signal(this).recurse(self -> self.previous()).take(size);
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
                .append(buyVolume())
                .append(" ")
                .append(sellVolume());

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return start.hashCode();
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
        return start.equals(other.start);
    }
}
