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
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 8:40:27
 */
public class Tick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /** Open price of the period */
    public final Decimal openPrice;

    /** Close price of the period */
    public Decimal closePrice = null;

    /** Max price of the period */
    public Decimal maxPrice = Decimal.ZERO;

    /** Min price of the period */
    public Decimal minPrice = Decimal.MAX;

    /** Traded amount during the period */
    public Decimal amount = Decimal.ZERO;

    /** Volume of the period */
    public Decimal volume = Decimal.ZERO;

    /**
     * Decode.
     * 
     * @param value
     */
    Tick(String value) {
        String[] values = value.split(" ");

        start = ZonedDateTime.parse(values[0]);
        end = ZonedDateTime.parse(values[1]);
        openPrice = Decimal.valueOf(values[2]);
        closePrice = Decimal.valueOf(values[3]);
        maxPrice = Decimal.valueOf(values[4]);
        minPrice = Decimal.valueOf(values[5]);
        volume = Decimal.valueOf(values[6]);
    }

    /**
    * 
    */
    Tick(ZonedDateTime start, ZonedDateTime end, Decimal open) {
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
        maxPrice = maxPrice.max(exe.price);
        minPrice = minPrice.min(exe.price);
        volume = volume.plus(exe.size);
    }

    /**
     * Assign date.
     * 
     * @param tick
     */
    void tick(Tick tick) {
        closePrice = tick.closePrice;
        maxPrice = maxPrice.max(tick.maxPrice);
        minPrice = minPrice.min(tick.minPrice);
        volume = volume.plus(tick.volume);
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
    public final Decimal getOpenPrice() {
        return openPrice;
    }

    /**
     * Get the closePrice property of this {@link Tick}.
     * 
     * @return The closePrice property.
     */
    public final Decimal getClosePrice() {
        return closePrice;
    }

    /**
     * Get the maxPrice property of this {@link Tick}.
     * 
     * @return The maxPrice property.
     */
    public final Decimal getMaxPrice() {
        return maxPrice;
    }

    /**
     * Get the minPrice property of this {@link Tick}.
     * 
     * @return The minPrice property.
     */
    public final Decimal getMinPrice() {
        return minPrice;
    }

    /**
     * Get the amount property of this {@link Tick}.
     * 
     * @return The amount property.
     */
    public final Decimal getAmount() {
        return amount;
    }

    /**
     * Get the volume property of this {@link Tick}.
     * 
     * @return The volume property.
     */
    public final Decimal getVolume() {
        return volume;
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
                .append(volume);

        return builder.toString();
    }
}
