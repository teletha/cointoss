/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import java.util.function.Function;

import cointoss.analyze.OnlineStats;
import cointoss.execution.Execution;
import cointoss.util.feather.FeatherStore;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

public final class Ticker implements Disposable {

    /** The span. */
    public final Span span;

    /** The event listeners. */
    final Signaling<Tick> opening = new Signaling();

    /** The event about opening new tick. */
    public final Signal<Tick> open = opening.expose;

    /** The event listeners. */
    final Signaling<Tick> closing = new Signaling();

    /** The event about opening new tick. */
    public final Signal<Tick> close = closing.expose;

    /** The tick store. */
    public final FeatherStore<Tick> ticks;

    /** The cache of upper tickers. */
    final Ticker[] uppers;

    /** The latest tick. */
    Tick current;

    /** The end time (epoch ms) of the latest tick. */
    private long currentTickEndTime;

    /** The realtime statistics for spread. */
    public final OnlineStats spreadStats = new OnlineStats();

    /** The realtime statistics for volume. */
    public final OnlineStats buyVolumeStats = new OnlineStats();

    /** The realtime statistics for volume. */
    public final OnlineStats sellVolumeStats = new OnlineStats();

    public final OnlineStats typicalStats = new OnlineStats();

    /** The realtime data holder. */
    final TickerManager manager;

    /**
     * Create {@link Ticker}.
     * 
     * @param span An associated span.
     */
    Ticker(Span span, TickerManager manager) {
        this(span, manager, null);
    }

    /**
     * Create {@link Ticker}.
     * 
     * @param span An associated span.
     */
    Ticker(Span span, TickerManager manager, Function<Span, FeatherStore<Tick>> store) {
        this.span = Objects.requireNonNull(span);
        this.uppers = new Ticker[span.uppers.length];
        this.ticks = store == null ? FeatherStore.create(Tick.class, span) : store.apply(span);
        this.manager = manager;

        if (manager != null && manager.service != null && span.sustainable) {
            ticks.enablePersistence(manager.service.formattedId, span.name());
        }
    }

    /**
     * Initialize {@link Ticker}.
     * 
     * @param execution The latest {@link Execution}.
     */
    final void init(Execution execution) {
        current = new Tick(span.calculateStartTime(execution.date).toEpochSecond(), execution.price, this);
        currentTickEndTime = computeEndTime();

        ticks.store(current);
        opening.accept(current);
    }

    /**
     * Add the new {@link Tick} if needed.
     * 
     * @param execution The latest {@link Execution}.
     * @return When the new {@link Tick} was added, this method will return <code>true</code>.
     */
    final boolean createTick(Execution execution) {
        // Make sure whether the execution does not exceed the end time of current tick.
        if (currentTickEndTime <= execution.mills) {
            // If the end time of current tick does not reach the start time of tick which
            // execution actually belongs to, it is assumed that there was a blank time
            // (i.e. server error, maintenance). So we complement them in advance.
            ZonedDateTime start = span.calculateStartTime(execution.date);

            while (current.openTime + span.seconds < start.toEpochSecond()) {
                current.freeze();
                closing.accept(current);
                current = new Tick(current.openTime + span.seconds, current.closePrice(), this);
                ticks.store(current);
            }

            // create the latest tick for execution
            current.freeze();
            closing.accept(current);
            current = new Tick(current.openTime + span.seconds, execution.price, this);
            currentTickEndTime = computeEndTime();
            ticks.store(current);
            opening.accept(current);
            return true;
        } else {
            return false; // end it immediately
        }
    }

    private long computeEndTime() {
        return (current.openTime + span.seconds) * 1000;
    }

    /**
     * Get the latest tick.
     */
    public Tick latest() {
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        ticks.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Ticker.class.getSimpleName() + "[" + span + "] " + ticks;
    }
}