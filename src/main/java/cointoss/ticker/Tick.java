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

import java.time.ZonedDateTime;

import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

public final class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** The previous tick. */
    public final Tick previous;

    /** Close price of the period. */
    Num closePrice;

    /** Max price of the period */
    Num highPrice;

    /** Min price of the period */
    Num lowPrice;

    /** The realtime execution statistic. */
    TickerManager realtime;

    /** Snapshot of long volume at tick initialization. */
    Num buyVolume;

    /** Snapshot of long price increase at tick initialization. */
    Num buyPriceIncrease;

    /** Snapshot of short volume at tick initialization. */
    Num sellVolume;

    /** Snapshot of short price decrease at tick initialization. */
    Num sellPriceDecrease;

    /**
     * New {@link Tick}.
     * 
     * @param start A start time of period.
     * @param span A tick span.
     * @param open A open price.
     * @param realtime The realtime execution statistic.
     */
    Tick(Tick previous, ZonedDateTime start, Span span, Num open, TickerManager realtime) {
        this.previous = previous;
        this.start = start;
        this.end = start.plus(span.duration);
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.realtime = realtime;
        this.buyVolume = realtime.longVolume;
        this.buyPriceIncrease = realtime.longPriceIncrease;
        this.sellVolume = realtime.shortVolume;
        this.sellPriceDecrease = realtime.shortPriceDecrease;
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
        return buyVolume().plus(sellVolume());
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num buyVolume() {
        return realtime == null ? buyVolume : realtime.longVolume.minus(buyVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num buyPriceIncrease() {
        return realtime == null ? buyPriceIncrease : realtime.longPriceIncrease.minus(buyPriceIncrease);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num sellVolume() {
        return realtime == null ? sellVolume : realtime.shortVolume.minus(sellVolume);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num sellPriceDecrease() {
        return realtime == null ? sellPriceDecrease : realtime.shortPriceDecrease.minus(sellPriceDecrease);
    }

    /**
     * @return
     */
    public Num priceVolatility() {
        Num upPotencial = buyVolume().isZero() ? Num.ZERO : buyPriceIncrease().divide(buyVolume());
        Num downPotencial = sellVolume().isZero() ? Num.ZERO : sellPriceDecrease().divide(sellVolume());
        return upPotencial.divide(downPotencial).scale(2);
    }

    public Num upRatio() {
        return buyVolume().isZero() ? Num.ZERO : buyPriceIncrease().multiply(buyVolume());
    }

    public Num downRatio() {
        return sellVolume().isZero() ? Num.ZERO : sellPriceDecrease().multiply(sellVolume());
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
        return I.signal(this).recurse(self -> self.previous).take(size);
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
}
