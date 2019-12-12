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

import cointoss.execution.Execution;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

public final class Ticker implements Disposable {

    /** Reusable NULL object. */
    public static final Ticker EMPTY = new Ticker(TimeSpan.Minute1);

    /** The span. */
    public final TimeSpan span;

    /** The event listeners. */
    final Signaling<Tick> additions = new Signaling();

    /** The event about adding new tick. */
    public final Signal<Tick> add = additions.expose;

    /** The event listeners. */
    final Signaling<Tick> updaters = new Signaling();

    /** The event about update tick. */
    public final Signal<Tick> update = updaters.expose;

    /** The tick store. */
    public final TimeseriesStore<Tick> ticks;

    /** The cache of upper tickers. */
    final Ticker[] uppers;

    /** The latest tick. */
    Tick current;

    /**
     * Create {@link Ticker}.
     * 
     * @param span An associated span.
     */
    Ticker(TimeSpan span) {
        this.span = Objects.requireNonNull(span);
        this.uppers = new Ticker[span.uppers.length];
        this.ticks = new TimeseriesStore<Tick>(span, tick -> tick.startSeconds);
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

        ticks.store(current);
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
                ticks.store(current);
            }

            // create the latest tick for execution
            current.freeze();
            current = new Tick(prev, current.end, span, execution.id, execution.delay, execution.price, realtime);
            ticks.store(current);
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Ticker.class.getSimpleName() + "[" + span + "] " + ticks;
    }
}
