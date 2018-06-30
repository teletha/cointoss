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

import org.magicwerk.brownies.collections.BigList;

import cointoss.Execution;
import kiss.Signal;
import kiss.Signaling;

/**
 * @version 2018/06/30 1:32:14
 */
public class Ticker2 {

    /** The span. */
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

    /** The latest tick. */
    Tick last = Tick.PAST;

    /**
     * @param span
     */
    public Ticker2(TickSpan span) {
        this.span = Objects.requireNonNull(span);
    }

    void update(Execution e) {
        if (!e.isBefore(last.end)) {
            ZonedDateTime start = span.calculateStartTime(e.exec_date);
            ZonedDateTime end = span.calculateEndTime(e.exec_date);

            last = new Tick(start, end, e.price);
            ticks.addLast(last);
        }
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Ticker2.class.getSimpleName() + "[" + span + "]";
    }
}