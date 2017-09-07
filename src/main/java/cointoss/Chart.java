/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.TimeSeries;

/**
 * @version 2017/09/05 18:40:19
 */
@SuppressWarnings("serial")
public class Chart extends TimeSeries {

    private final Duration duration;

    private final Chart child;

    private BaseTick current;

    private final List<Consumer<BaseTick>> listeners = new CopyOnWriteArrayList();

    /**
     * 
     */
    Chart(Duration duration, Chart child) {
        super(duration.toString());

        this.duration = duration;
        this.child = child;
        this.current = new BaseTick(duration);
    }

    /**
     * Record executions.
     */
    void tick(Execution exe) {
        if (exe.exec_date.isBefore(current.endTime)) {
            current.mark(exe);
        } else {
            addTick(current);
            if (child != null) {
                child.tick(current);
            }

            for (Consumer<BaseTick> listener : listeners) {
                listener.accept(current);
            }
            current = new BaseTick(exe, duration);
        }
    }

    /**
     * Record tick.
     */
    private void tick(BaseTick tick) {
        if (tick.endTime.isBefore(current.endTime)) {
            current.mark(tick);
        } else {
            addTick(current);
            if (child != null) {
                child.tick(current);
            }

            for (Consumer<BaseTick> listener : listeners) {
                listener.accept(current);
            }

            current = new BaseTick(tick, duration);
        }
    }

    /**
     * Observe tick.
     * 
     * @param object
     */
    public void to(Consumer<BaseTick> listener) {
        listeners.add(listener);
    }
}
