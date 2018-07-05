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
 * @version 2018/07/05 10:16:49
 */
public final class Ticker {

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

    /** The cache of upper tickers. */
    final Ticker[] uppers;

    /** The latest tick. */
    Tick current;

    /** The lock system. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Create {@link Ticker}.
     * 
     * @param span An associated span.
     */
    Ticker(TickSpan span) {
        this.span = Objects.requireNonNull(span);
        this.uppers = new Ticker[span.uppers.length];
    }

    /**
     * Initialize {@link Ticker}.
     * 
     * @param execution The latest {@link Execution}.
     * @param realtime The realtime execution statistic.
     */
    final void init(Execution execution, TickerManager realtime) {
        current = new Tick(span.calculateStartTime(execution.exec_date), span, execution.price, realtime);

        ticks.addLast(current);
        additions.accept(current);
    }

    /**
     * Add the new {@link Tick} if needed.
     * 
     * @param execution The latest {@link Execution}.
     * @param realtime The realtime execution statistic.
     * @return When the new {@link Tick} was added, this method will return <code>true</code>.
     */
    final boolean createTick(Execution execution, TickerManager realtime) {
        // Make sure whether the execution does not exceed the end time of current tick.
        if (!execution.isBefore(current.end)) {
            lock.writeLock().lock();

            try {
                // If the end time of current tick does not reach the start time of tick which
                // execution actually belongs to, it is assumed that there was a blank time
                // (i.e. server error, maintenance). So we complement them in advance.
                ZonedDateTime start = span.calculateStartTime(execution.exec_date);

                while (current.end.isBefore(start)) {
                    current.freeze();
                    current = new Tick(current.end, span, current.closePrice(), realtime);
                    ticks.addLast(current);
                    additions.accept(current);
                }

                // create the latest tick for execution
                current.freeze();
                current = new Tick(current.end, span, execution.price, realtime);
                ticks.addLast(current);
                additions.accept(current);
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        } else {
            return false; // end it immediately
        }
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
        return ticks.peekFirst();
    }

    /**
     * Get last tick.
     * 
     * @return
     */
    public final Tick last() {
        return ticks.peekLast();
    }

    /**
     * Calculate the {@link Tick} size.
     * 
     * @return
     */
    public final int size() {
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