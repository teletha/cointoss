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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import cointoss.Execution;
import cointoss.chart.simple.PriceIndicator;
import cointoss.util.RingBuffer;
import eu.verdelhan.ta4j.Decimal;
import filer.Filer;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/09/07 22:12:13
 */
@SuppressWarnings("serial")
public class Chart {

    /** The chart duration. */
    private final Duration duration;

    /** The current tick */
    private Tick current;

    /** The tick manager. */
    public final RingBuffer<Tick> ticks = new RingBuffer(60 * 24);

    /** The tick observers. */
    private final CopyOnWriteArrayList<Observer<? super Tick>> listeners = new CopyOnWriteArrayList<>();

    /** The tick observers. */
    public final Signal<Tick> tick = new Signal(listeners);

    /** The trend indicator. */
    private final Indicator trend;

    /**
     * 
     */
    public Chart(Duration duration, Chart... children) {
        this.duration = duration;
        this.trend = PriceIndicator.weightMedian(this).sma(30);

        for (Chart child : children) {
            tick.to(child::tick);
        }
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public Trend trend() {
        Decimal latest = trend.getLast(0);
        Decimal total = Decimal.ONE;

        for (int i = 1; i < Math.min(24, ticks.size()); i++) {
            Decimal ratio = latest.dividedBy(trend.getLast(i));
            total = total.multipliedBy(ratio);
        }
        return total.isLessThan(Decimal.valueOf("0.65")) ? Trend.Down
                : total.isGreaterThan(Decimal.valueOf("1.35")) ? Trend.Up : Trend.Range;
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isUpTrend() {
        return trend() == Trend.Up;
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isDownTrend() {
        return trend() == Trend.Down;
    }

    /**
     * Detect trend.
     * 
     * @return
     */
    public boolean isRange() {
        return trend() == Trend.Range;
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
     * Return the latest tick.
     * 
     * @return
     */
    public Tick getLastTick() {
        return ticks.latest();
    }

    /**
     * @param index
     * @return
     */
    public Tick getTick(int index) {
        return ticks.get(index);
    }

    /**
     * Record executions.
     */
    public void tick(Execution exe) {
        if (current == null) {
            ticks.add(current = convert(exe));
        }

        if (!exe.exec_date.isBefore(current.end)) {
            // notify
            for (Observer<? super Tick> listener : listeners) {
                listener.accept(current);
            }

            // update
            ticks.add(current = convert(exe));
        }
        current.tick(exe);
    }

    /**
     * @param exe
     * @return
     */
    private Tick convert(Execution exe) {
        ZonedDateTime time = exe.exec_date.withNano(0);
        long epochSecond = time.toEpochSecond();
        epochSecond = epochSecond - (epochSecond % duration.getSeconds());
        ZonedDateTime normalized = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), time.getZone());

        return new Tick(normalized, normalized.plus(duration), exe.price);
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
        current.tick(tick);
    }

    /**
     * <p>
     * Write out the current tick log to the specified file
     * </p>
     * 
     * @param file
     */
    public void writeTo(Path file) {
        List<String> list = I.signal(IntStream.range(ticks.start(), ticks.end())).map(this::getTick).map(tick -> tick.toString()).toList();

        try {
            Files.createDirectories(file.getParent());
            Files.write(file, list);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Read tick log from the specified file.
     * </p>
     * 
     * @param file
     */
    public void readFrom(Path file) {
        Filer.read(file).map(line -> new Tick(line)).to(ticks::add);
    }

    /**
     * Observe tick.
     * 
     * @param object
     */
    public void to(Observer<? super Tick> listener) {
        listeners.add(listener);
    }

    /**
     * Observe tick.
     * 
     * @return
     */
    public Signal<Tick> signal() {
        return new Signal<>(listeners);
    }
}
