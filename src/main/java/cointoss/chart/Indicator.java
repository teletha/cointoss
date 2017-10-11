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

import cointoss.util.Num;
import cointoss.util.RingBuffer;

/**
 * @version 2017/09/10 12:18:58
 */
public abstract class Indicator {

    /** The internal cache. */
    private final RingBuffer<Num> cache;

    /** The target chart. */
    protected final Chart chart;

    /**
     * @param chart
     */
    protected Indicator(Chart chart) {
        this.chart = chart;
        this.cache = new RingBuffer(chart.ticks, chart + "-" + toString());
    }

    /**
     * Return the index (from first) value.
     * 
     * @param index
     * @return
     */
    public final Num get(int index) {
        int end = chart.ticks.end() - 1;

        if (index == end) {
            return calculate(end);
        } else {
            return cache.get(index, this::calculate);
        }
    }

    /**
     * Return the index (from last) value.
     */
    public final Num getLast(int lastIndex) {
        return get(chart.ticks.end() - lastIndex - 1);
    }

    /**
     * Calculate the indexed value.
     * 
     * @param index
     * @return
     */
    public abstract Num calculate(int index);

    /**
     * Compose Simple Moving Average indicator.
     * 
     * @param timeFrame
     * @return
     */
    public Indicator sma(int timeFrame) {
        return new ComposableIndicator(this) {
            /**
             * {@inheritDoc}
             */
            @Override
            public Num calculate(int index) {
                Num sum = Num.ZERO;

                for (int i = Math.max(0, index - timeFrame + 1); i <= index; i++) {
                    sum = sum.plus(indicator.get(i));
                }

                final int realTimeFrame = Math.min(timeFrame, index + 1);
                return sum.divide(Num.of(realTimeFrame));
            }
        };
    }

    /**
     * Compose Exponential Moving Average indicator.
     * 
     * @param timeFrame
     * @return
     */
    public Indicator ema(int timeFrame) {
        Num multiplier = Num.TWO.divide(Num.of(timeFrame + 1));

        return new ComposableIndicator(this) {
            /**
             * {@inheritDoc}
             */
            @Override
            public Num calculate(int index) {
                if (index + 1 < timeFrame) {
                    // Starting point of the EMA
                    return sma(timeFrame).get(index);
                }
                if (index == 0) {
                    // If the timeframe is bigger than the indicator's value count
                    return indicator.get(0);
                }
                Num emaPrev = get(index - 1);
                return indicator.get(index).minus(emaPrev).multiply(multiplier).plus(emaPrev);
            }
        };
    }
}
