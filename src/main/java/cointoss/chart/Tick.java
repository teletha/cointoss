/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import java.time.ZonedDateTime;

import cointoss.Execution;
import cointoss.util.Num;

/**
 * @version 2017/09/10 8:40:27
 */
public class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Num openPrice;

    /** Close price of the period */
    public Num closePrice = null;

    /** Max price of the period */
    public Num maxPrice = Num.ZERO;

    /** Min price of the period */
    public Num minPrice = Num.MAX;

    /** Volume of the period */
    public Num volume = Num.ZERO;

    /** Traded amount during the period */
    public Num amount = Num.ZERO;

    /** Traded amount during the period */
    public Num amountSquare = Num.ZERO;

    /**
     * Decode.
     * 
     * @param value
     */
    Tick(String value) {
        String[] values = value.split(" ");

        start = ZonedDateTime.parse(values[0]);
        end = ZonedDateTime.parse(values[1]);
        openPrice = Num.of(values[2]);
        closePrice = Num.of(values[3]);
        maxPrice = Num.of(values[4]);
        minPrice = Num.of(values[5]);
        volume = Num.of(values[6]);
        amount = Num.of(values[7]);
    }

    /**
    * 
    */
    Tick(ZonedDateTime start, ZonedDateTime end, Num open) {
        this.start = start;
        this.end = end;
        this.openPrice = open;
    }

    /**
     * Assign date.
     * 
     * @param exe
     */
    void tick(Execution exe) {
        closePrice = exe.price;
        maxPrice = Num.max(maxPrice, exe.price);
        minPrice = Num.min(minPrice, exe.price);
        volume = volume.plus(exe.size);
        amount = amount.plus(exe.price.multiply(exe.size));
    }

    /**
     * Assign date.
     * 
     * @param tick
     */
    void tick(Tick tick) {
        closePrice = tick.closePrice;
        maxPrice = Num.max(maxPrice, tick.maxPrice);
        minPrice = Num.min(minPrice, tick.minPrice);
        volume = volume.plus(tick.volume);
        amount = amount.plus(tick.amount);
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
     * Get the closePrice property of this {@link Tick}.
     * 
     * @return The closePrice property.
     */
    public final Num getClosePrice() {
        return closePrice;
    }

    /**
     * Get the maxPrice property of this {@link Tick}.
     * 
     * @return The maxPrice property.
     */
    public final Num getMaxPrice() {
        return maxPrice;
    }

    /**
     * Get the minPrice property of this {@link Tick}.
     * 
     * @return The minPrice property.
     */
    public final Num getMinPrice() {
        return minPrice;
    }

    /**
     * Get the amount property of this {@link Tick}.
     * 
     * @return The amount property.
     */
    public final Num getAmount() {
        return amount;
    }

    /**
     * Get the volume property of this {@link Tick}.
     * 
     * @return The volume property.
     */
    public final Num getVolume() {
        return volume;
    }

    /**
     * @return
     */
    public final Num getWeightMedian() {
        return amount.divide(volume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(start)
                .append(" ")
                .append(end)
                .append(" ")
                .append(openPrice)
                .append(" ")
                .append(closePrice)
                .append(" ")
                .append(maxPrice)
                .append(" ")
                .append(minPrice)
                .append(" ")
                .append(volume)
                .append(" ")
                .append(amount);

        return builder.toString();
    }
}
