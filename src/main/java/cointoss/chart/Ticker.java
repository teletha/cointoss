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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.magicwerk.brownies.collections.BigList;

import cointoss.util.Listeners;
import kiss.Signal;

/**
 * @version 2018/01/29 9:41:02
 */
public class Ticker {

    /** The event listeners. */
    private final Listeners<Tick> listeners = new Listeners();

    /** The event about adding new tick. */
    public final Signal<Tick> add = new Signal(listeners);

    /** The tick manager. */
    private final BigList<Tick> ticks = new BigList();

    /** The lock system. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 
     */
    public Ticker(Signal<Tick> signal) {
        signal.diff().delay(1).to(tick -> {
            lock.writeLock().lock();

            try {
                ticks.add(tick);
                listeners.omit(tick);
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    /**
     * Calculate start time.
     * 
     * @return
     */
    public final ZonedDateTime startTime() {
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.getFirst().start;
    }

    /**
     * Calculate end time.
     * 
     * @return
     */
    public final ZonedDateTime endTime() {
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.getLast().end;
    }

    /**
     * Calculate tick size.
     * 
     * @return
     */
    public final int size() {
        return ticks.size();
    }

    /**
     * Get latest tick.
     * 
     * @return
     */
    public final Tick latest() {
        return ticks.getLast();
    }

    /**
     * List up all ticks.
     * 
     * @param consumer
     */
    public final void each(Consumer<Tick> consumer) {
        lock.readLock().lock();

        try {
            for (Tick tick : ticks) {
                consumer.accept(tick);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
