/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart.simple;

import java.util.function.Function;

import cointoss.chart.Chart;
import cointoss.chart.Indicator;
import cointoss.chart.Tick;
import cointoss.util.Num;

/**
 * @version 2017/09/10 13:00:35
 */
public class PriceIndicator extends Indicator {

    /** The price calculator. */
    private final Function<Tick, Num> calculator;

    /**
     * @param chart
     */
    public PriceIndicator(Chart chart, Function<Tick, Num> calculator) {
        super(chart);

        this.calculator = calculator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Num calculate(int index) {
        return calculator.apply(chart.ticks.get(index));
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator close(Chart chart) {
        return new PriceIndicator(chart, Tick::getClosePrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator open(Chart chart) {
        return new PriceIndicator(chart, Tick::getOpenPrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator max(Chart chart) {
        return new PriceIndicator(chart, Tick::getMaxPrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator min(Chart chart) {
        return new PriceIndicator(chart, Tick::getMinPrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator weightMedian(Chart chart) {
        return new PriceIndicator(chart, Tick::getWeightMedian);
    }
}
