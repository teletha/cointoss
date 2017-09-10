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
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 13:00:35
 */
public class PriceIndicator extends Indicator<Decimal> {

    /** The price calculator. */
    private final Function<Tick, Decimal> calculator;

    /**
     * @param chart
     */
    public PriceIndicator(Chart chart, Function<Tick, Decimal> calculator) {
        super(chart);

        this.calculator = calculator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal calculate(int index) {
        return calculator.apply(chart.ticks.get(index));
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator close(Chart chart) {
        return new PriceIndicator(chart, tick -> tick.closePrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator open(Chart chart) {
        return new PriceIndicator(chart, tick -> tick.openPrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator max(Chart chart) {
        return new PriceIndicator(chart, tick -> tick.maxPrice);
    }

    /**
     * Helper to create price related indicator.
     * 
     * @param chart
     * @return
     */
    public static final PriceIndicator min(Chart chart) {
        return new PriceIndicator(chart, tick -> tick.minPrice);
    }
}
