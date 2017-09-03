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

import java.util.ArrayList;
import java.util.List;

import cointoss.indicator.simple.ClosePriceIndicator;
import cointoss.indicator.trackers.MACDIndicator;
import kiss.I;

/**
 * @version 2017/08/22 14:28:23
 */
public class TimeSeries {

    /** The current tick. */
    private Tick current = new Tick();

    /** The records. */
    private List<Tick> ticks = new ArrayList();

    /**
     * Get the current tick size.
     * 
     * @return
     */
    public int size() {
        return ticks.size();
    }

    /**
     * Record executions.
     */
    public void tick(Execution exe) {
        if (exe.exec_date.isBefore(current.endTime)) {
            current.mark(exe);
        } else {
            ticks.add(current);
            current = new Tick(exe);
        }
    }

    /**
     * 
     */
    public Tick latest() {
        return current;
    }

    /**
     * @return
     */
    public List<Tick> getTicks() {
        return ticks;
    }

    /**
     * @param index
     * @return
     */
    public Tick getTick(int index) {
        return ticks.get(index);
    }

    /** CACHE */
    private MACDIndicator macd;

    /**
     * 
     */
    public MACDIndicator macd() {
        if (macd == null) {
            macd = new MACDIndicator(new ClosePriceIndicator(this), 9, 26);
        }
        return macd;
    }

    /**
     * @param builder
     */
    public static TimeSeries load(Class<? extends MarketBuilder> builder) {
        TimeSeries ticks = new TimeSeries();

        I.make(builder).initialize().to(ticks::tick);

        return ticks;
    }
}
