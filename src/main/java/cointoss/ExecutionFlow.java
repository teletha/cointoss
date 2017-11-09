/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;

import cointoss.util.Num;
import cointoss.util.RingBuffer;

/**
 * @version 2017/11/09 11:19:47
 */
public class ExecutionFlow {

    public int size = 0;

    /** The latest price. */
    public Num price = Num.ZERO;

    /** The volume. */
    public Num volume = Num.ZERO;

    /** Volume of the period */
    public Num longVolume = Num.ZERO;

    /** Volume of the period */
    public Num longPriceIncrese = Num.ZERO;

    /** Volume of the period */
    public Num shortVolume = Num.ZERO;

    /** Volume of the period */
    public Num shortPriceDecrease = Num.ZERO;

    /** The execution buffer. */
    private final RingBuffer<Execution> buffer;

    private ZonedDateTime next = ZonedDateTime.of(1971, 1, 1, 0, 0, 0, 0, Execution.UTC);

    private final long interval;

    public final RingBuffer<ExecutionFlow> history = new RingBuffer(60, "HISTORY");

    public int id;

    /**
     * @param i
     */
    public ExecutionFlow(int size, int mills) {
        this.buffer = new RingBuffer(size, "TREND" + size);
        this.interval = mills * 1000000;
    }

    /**
     * Record {@link Execution}.
     * 
     * @param exe
     */
    public void record(Execution exe) {
        if (exe.isAfter(next)) {
            history.add(copy());
            next = exe.exec_date.withNano(0).plusSeconds(1);
        }

        size++;

        Execution removed = buffer.add(exe);

        volume = volume.plus(exe.size);

        if (exe.side.isBuy()) {
            longVolume = longVolume.plus(exe.size);
            longPriceIncrese = longPriceIncrese.plus(exe.longPriceIncrese = exe.price.minus(price).multiply(exe.size));
        } else {
            shortVolume = shortVolume.plus(exe.size);
            shortPriceDecrease = shortPriceDecrease.plus(exe.shortPriceDecrease = price.minus(exe.price).multiply(exe.size));
        }

        if (removed != null) {
            volume = volume.minus(removed.size);

            if (removed.side.isBuy()) {
                longVolume = longVolume.minus(removed.size);
                longPriceIncrese = longPriceIncrese.minus(removed.longPriceIncrese);
            } else {
                shortVolume = shortVolume.minus(removed.size);
                shortPriceDecrease = shortPriceDecrease.minus(removed.shortPriceDecrease);
            }
        }

        price = exe.price;
    }

    /**
     * Compute volume diff.
     * 
     * @return
     */
    public Num volume() {
        return longVolume.minus(shortVolume);
    }

    /**
     * Compute volume diff.
     * 
     * @return
     */
    public Num power() {
        return longPriceIncrese.minus(shortPriceDecrease);
    }

    private ExecutionFlow copy() {
        ExecutionFlow copy = new ExecutionFlow(0, 0);
        copy.id = id++;
        copy.size = size;
        copy.price = price;
        copy.volume = volume;
        copy.longVolume = longVolume;
        copy.longPriceIncrese = longPriceIncrese;
        copy.shortVolume = shortVolume;
        copy.shortPriceDecrease = shortPriceDecrease;

        return copy;
    }
}
