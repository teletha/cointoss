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

    /** The latest execution. */
    public Execution latest = new Execution();

    /** The volume. */
    public Num volume = Num.ZERO;

    /** Volume of the period */
    public Num longVolume = Num.ZERO;

    /** Volume of the period */
    public Num shortVolume = Num.ZERO;

    public int longDown = 0;

    public int shortUp = 0;

    /** The execution buffer. */
    private final RingBuffer<Execution> buffer;

    private ZonedDateTime next = ZonedDateTime.of(1971, 1, 1, 0, 0, 0, 0, Execution.UTC);

    public final RingBuffer<ExecutionFlow> history = new RingBuffer(60, "HISTORY");

    public int id;

    /**
     * @param size
     */
    public ExecutionFlow(int size) {
        this.buffer = new RingBuffer(size, "TREND" + size);

        // initialize empty object
        latest.price = Num.ZERO;
    }

    /**
     * Record {@link Execution}.
     * 
     * @param exe
     */
    public void record(Execution exe) {
        if (!exe.isBefore(next)) {
            next = exe.exec_date.withNano(0).plusSeconds(1);

            history.add(copy());
        }

        size++;

        Execution removed = buffer.add(exe);

        volume = volume.plus(exe.size);

        if (exe.side.isBuy()) {
            longVolume = longVolume.plus(exe.size);

        } else {
            shortVolume = shortVolume.plus(exe.size);
        }

        if (removed != null) {
            volume = volume.minus(removed.size);

            if (removed.side.isBuy()) {
                longVolume = longVolume.minus(removed.size);
            } else {
                shortVolume = shortVolume.minus(removed.size);
            }
        }
        latest = exe;
    }

    /**
     * Compute volume diff.
     * 
     * @return
     */
    public Num volume() {
        return longVolume.minus(shortVolume);
    }

    private ExecutionFlow copy() {
        ExecutionFlow copy = new ExecutionFlow(0);
        copy.id = id++;
        copy.size = size;
        copy.latest = latest;
        copy.volume = volume;
        copy.longVolume = longVolume;
        copy.shortVolume = shortVolume;

        return copy;
    }
}
