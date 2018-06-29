/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import java.time.ZonedDateTime;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.magicwerk.brownies.collections.BigList;

import cointoss.Execution;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2018/06/27 15:46:14
 */
public class Ticker {

    /** Reusable NULL object.. */
    public static final Ticker EMPTY = new Ticker(TickSpan.Minute1, Signal.EMPTY);

    /** The target span. */
    public final TickSpan span;

    /** The event listeners. */
    private final Signaling<Tick> additions = new Signaling();

    /** The event about adding new tick. */
    public final Signal<Tick> add = additions.expose;

    /** The event listeners. */
    private final Signaling<Tick> updaters = new Signaling();

    /** The event about update tick. */
    public final Signal<Tick> update = updaters.expose;

    /** The tick manager. */
    private final BigList<Tick> ticks = new BigList();

    /** The lock system. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 
     */
    public Ticker(TickSpan span, Signal<Execution> timeline) {
        this.span = span;

        timeline.map(Tick.by(span)).effect(updaters).diff().to(tick -> {
            lock.writeLock().lock();

            try {
                // handle unobservable ticks (i.e. server error, maintenance)
                Tick last = last();

                if (last != null) {
                    ZonedDateTime nextStart = last.start.plus(span.duration);

                    while (nextStart.isBefore(tick.start)) {
                        // some ticks were skipped by unknown error, so we will complement
                        last = new Tick(nextStart, nextStart.plus(span.duration), last.openPrice);
                        last.closePrice = last.highPrice = last.lowPrice = last.openPrice;
                        ticks.add(last);

                        nextStart = last.end;
                    }
                }

                ticks.add(tick);
                additions.accept(tick);
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
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.peekFirst().start;
    }

    /**
     * Calculate end time.
     * 
     * @return
     */
    public final ZonedDateTime endTime() {
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.peekLast().end;
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
     * Get first tick.
     * 
     * @return
     */
    public final Tick first() {
        Tick tick = ticks.peekFirst();
        return tick == null ? Tick.NOW : tick;
    }

    /**
     * Get last tick.
     * 
     * @return
     */
    public final Tick last() {
        Tick tick = ticks.peekLast();
        return tick == null ? Tick.NOW : tick;
    }

    /**
     * List up all ticks.
     * 
     * @param consumer
     */
    public final void each(Consumer<Tick> consumer) {
        each(0, size(), consumer);
    }

    /**
     * List up all ticks.
     * 
     * @param consumer
     */
    public final void each(int start, int size, Consumer<Tick> consumer) {
        lock.readLock().lock();

        size = Math.min(start + size, ticks.size());

        try {
            for (int i = start; i < size; i++) {
                consumer.accept(ticks.get(i));
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Find by epoch second.
     * 
     * @param epochSeconds
     * @return
     */
    public final Variable<Tick> findByEpochSecond(long epochSeconds) {
        lock.readLock().lock();

        try {
            int index = (int) ((epochSeconds - first().start.toEpochSecond()) / span.duration.getSeconds());
            return index < size() ? Variable.of(ticks.get(index)) : Variable.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
}
