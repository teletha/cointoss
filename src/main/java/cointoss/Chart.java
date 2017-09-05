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

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

/**
 * @version 2017/09/05 18:40:19
 */
public class Chart extends TimeSeries {

    private final Duration duration;

    private final Chart child;

    private Tick current;

    /**
     * 
     */
    Chart(Duration duration, Chart child) {
        super(duration.toString());

        this.duration = duration;
        this.child = child;
        this.current = new Tick(duration);
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
            current = new Tick(exe, duration);
        }
    }

    /**
     * Record tick.
     */
    private void tick(Tick tick) {
        if (tick.endTime.isBefore(current.endTime)) {
            current.mark(tick);
        } else {
            addTick(current);
            if (child != null) {
                child.tick(current);
            }
            current = new Tick(tick, duration);
        }
    }
}
