/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.indicator.trackers;

import cointoss.Amount;
import cointoss.indicator.AbstractIndicator;
import cointoss.indicator.Indicator;

/**
 * Exponential moving average indicator.
 * 
 * @version 2017/08/25 23:51:34
 */
public class ExponentialMovingAverageIndicator extends AbstractIndicator<Amount> {

    /**
     * 
     */
    private static final long serialVersionUID = 6269888228868638961L;

    private final Indicator<Amount> indicator;

    private final int tickSize;

    private final Amount multiplier;

    /**
     * @param indicator
     * @param tickSize
     */
    public ExponentialMovingAverageIndicator(Indicator<Amount> indicator, int tickSize) {
        super(indicator.getTimeSeries());

        this.indicator = indicator;
        this.tickSize = tickSize;
        this.multiplier = Amount.TWO.divide(Amount.of(tickSize + 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        if (index + 1 < tickSize) {
            // Starting point of the EMA
            return new SimpleMovingAverageIndicator(indicator, tickSize).getValue(index);
        }

        if (index == 0) {
            // If the timeframe is bigger than the indicator's value count
            return indicator.getValue(0);
        }
        Amount previous = getValue(index - 1);
        return indicator.getValue(index).minus(previous).multiply(multiplier).plus(previous);
    }
}
