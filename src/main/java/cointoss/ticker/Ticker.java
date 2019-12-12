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
import java.util.Objects;
import java.util.function.Consumer;

import cointoss.execution.Execution;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public final class Ticker implements Disposable {

    /** Reusable NULL object.. */
    public static final Ticker EMPTY = new Ticker(Span.Minute1);

    /** The span. */
    public final Span span;

    /** The event listeners. */
    final Signaling<Tick> additions = new Signaling();

    /** The event about adding new tick. */
    public final Signal<Tick> add = additions.expose;

    /** The event listeners. */
    final Signaling<Tick> updaters = new Signaling();

    /** The event about update tick. */
    public final Signal<Tick> update = updaters.expose;

    /** The tick manager. */
    final SegmentBuffer<Tick> ticks;

    /** The cache of upper tickers. */
    final Ticker[] uppers;

    /** The latest tick. */
    Tick current;

    /**
     * Create {@link Ticker}.
     * 
     * @param span An associated span.
     */
    Ticker(Span span) {
        this.span = Objects.requireNonNull(span);
        this.uppers = new Ticker[span.uppers.length];
        this.ticks = new SegmentBuffer<Tick>(span.ticksPerDay(), tick -> tick.start.toLocalDate());
    }

    /**
     * Initialize {@link Ticker}.
     * 
     * @param execution The latest {@link Execution}.
     * @param realtime The realtime execution statistic.
     */
    final void init(Execution execution, TickerManager realtime) {
        current = new Tick(ticks.last(), span
                .calculateStartTime(execution.date), span, execution.id, execution.delay, execution.price, realtime);

        ticks.add(current);
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

            // If the end time of current tick does not reach the start time of tick which
            // execution actually belongs to, it is assumed that there was a blank time
            // (i.e. server error, maintenance). So we complement them in advance.
            ZonedDateTime start = span.calculateStartTime(execution.date);
            Tick prev = ticks.last();

            while (current.end.isBefore(start)) {
                current.freeze();
                current = new Tick(prev, current.end, span, execution.id, execution.delay, current.closePrice(), realtime);
                ticks.add(current);
            }

            // create the latest tick for execution
            current.freeze();
            current = new Tick(prev, current.end, span, execution.id, execution.delay, execution.price, realtime);
            ticks.add(current);
            additions.accept(current);
            return true;
        } else {
            return false; // end it immediately
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        ticks.clear();
    }

    /**
     * Calculate start time.
     * 
     * @return
     */
    public final ZonedDateTime startTime() {
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.first().start;
    }

    /**
     * Calculate end time.
     * 
     * @return
     */
    public final ZonedDateTime endTime() {
        return ticks.isEmpty() ? ZonedDateTime.now() : ticks.last().end;
    }

    /**
     * Get first tick.
     * 
     * @return
     */
    public final Tick first() {
        return ticks.first();
    }

    /**
     * Get last tick.
     * 
     * @return
     */
    public final Tick last() {
        return ticks.last();
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
     * Retrieve {@link Tick} at the specified index.
     * 
     * @param index
     * @return
     */
    public final Tick get(int index) {
        return ticks.get(index);
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
        ticks.each(start, Math.min(start + size, ticks.size()), consumer);
    }

    /**
     * Find by epoch second.
     * 
     * @param epochSeconds
     * @return
     */
    public final Variable<Tick> findByEpochSecond(long epochSeconds) {
        if (ticks.isEmpty()) {
            return Variable.empty();
        }
        int index = (int) ((epochSeconds - first().start.toEpochSecond()) / span.duration.getSeconds());
        return 0 <= index && index < size() ? Variable.of(ticks.get(index)) : Variable.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Ticker.class.getSimpleName() + "[" + span + "] " + ticks;
    }
}
