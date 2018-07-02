/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.magicwerk.brownies.collections.BigList;

import cointoss.Execution;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2018/06/30 1:32:14
 */
public class Ticker {

    /** Reusable NULL object.. */
    public static final Ticker EMPTY = new Ticker(TickSpan.Minute1);

    /** The span. */
    public final TickSpan span;

    /** The event listeners. */
    final Signaling<Tick> additions = new Signaling();

    /** The event about adding new tick. */
    public final Signal<Tick> add = additions.expose;

    /** The event listeners. */
    final Signaling<Tick> updaters = new Signaling();

    /** The event about update tick. */
    public final Signal<Tick> update = updaters.expose;

    /** The tick manager. */
    final BigList<Tick> ticks = new BigList();

    /** The latest tick. */
    Tick last = Tick.PAST;

    /** The lock system. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * @param span
     */
    public Ticker(TickSpan span) {
        this.span = Objects.requireNonNull(span);
    }

    boolean update(Execution e, BaseStatistics base) {
        boolean created = false;

        if (!e.isBefore(last.end)) {
            lock.writeLock().lock();

            try {
                ZonedDateTime start = span.calculateStartTime(e.exec_date);
                ZonedDateTime end = span.calculateEndTime(e.exec_date);
                if (last != Tick.PAST) {
                    // handle unobservable ticks (i.e. server error, maintenance)
                    ZonedDateTime nextStart = last.start.plus(span.duration);

                    while (nextStart.isBefore(start)) {
                        last.snapshot = base.snapshot();
                        last.base = null;

                        // some ticks were skipped by unknown error, so we will complement
                        last = new Tick(nextStart, nextStart.plus(span.duration), last.openPrice);
                        ticks.addLast(last);
                        additions.accept(last);

                        nextStart = last.end;
                    }
                }
                last.snapshot = base.snapshot();
                last.base = null;
                last = new Tick(start, end, e.price);
                last.base = base;
                last.snapshot = base.snapshot();
                ticks.addLast(last);
                additions.accept(last);
                created = true;
            } finally {
                lock.writeLock().unlock();
            }
        }
        return created;
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
     * @return
     */
    public int size() {
        return ticks.size();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Ticker.class.getSimpleName() + "[" + span + "] " + ticks;
    }
}