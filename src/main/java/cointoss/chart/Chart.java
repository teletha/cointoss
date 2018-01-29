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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.magicwerk.brownies.collections.BigList;

import cointoss.Execution;
import cointoss.util.Listeners;
import cointoss.util.Num;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/09/07 22:12:13
 */
public class Chart {

    /** The chart duration. */
    private final Duration duration;

    /** The current tick */
    private Tick current;

    /** The tick manager. */
    public final BigList<Tick> ticks = new BigList();

    /** The tick observers. */
    private final Listeners<Tick> listeners = new Listeners();

    public final Signal<Tick> add = new Signal(listeners);

    /** The tick observers. */
    public final Signal<Tick> tick = new Signal(listeners);

    /**
     * 
     */
    public Chart(Duration duration, Chart... children) {
        this.duration = duration;

        for (Chart child : children) {
            tick.to(child::tick);
        }
    }

    /**
     * <p>
     * Return the current tick size.
     * </p>
     * 
     * @return
     */
    public int getTickCount() {
        return ticks.size();
    }

    /**
     * Return the first tick.
     * 
     * @return
     */
    public final Tick getFirstTick() {
        return ticks.getFirst();
    }

    /**
     * Return the latest tick.
     * 
     * @return
     */
    public final Tick getLastTick() {
        return ticks.getLast();
    }

    /**
     * @param fromFirst
     * @return
     */
    public final Tick getTick(int fromFirst) {
        return ticks.get(fromFirst);
    }

    /**
     * Return the latest tick.
     * 
     * @return
     */
    public final Tick getLatestTick(int fromLast) {
        return ticks.get(ticks.size() - 1 - fromLast);
    }

    /**
     * Record executions.
     */
    public void tick(Execution exe) {
        if (current == null) {
            ticks.add(current = convert(exe, exe.price));
        }

        if (!exe.exec_date.isBefore(current.end)) {
            listeners.omit(current);

            // update
            ticks.add(current = convert(exe, current.closePrice));
        }
        current.update(exe);
    }

    /**
     * @param exe
     * @return
     */
    private Tick convert(Execution exe, Num previousPrice) {
        ZonedDateTime time = exe.exec_date.withNano(0);
        long epochSecond = time.toEpochSecond();
        epochSecond = epochSecond - (epochSecond % duration.getSeconds());
        ZonedDateTime normalized = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), time.getZone());

        return new Tick(normalized, normalized.plus(duration), previousPrice);
    }

    /**
     * Record executions.
     */
    private void tick(Tick tick) {
        if (current == null) {
            current = new Tick(tick.start, tick.start.plus(duration), tick.openPrice);
            ticks.add(current);
        }

        if (!tick.start.isBefore(current.end)) {
            // notify
            for (Observer<? super Tick> listener : listeners) {
                listener.accept(current);
            }

            // update
            current = new Tick(tick.start, tick.start.plus(duration), tick.openPrice);
            ticks.add(current);
        }
        current.update(tick);
    }

    /**
     * Observe tick.
     * 
     * @return
     */
    public Signal<Tick> signal() {
        return new Signal<>(listeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return LocalTime.MIDNIGHT.plus(duration).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
