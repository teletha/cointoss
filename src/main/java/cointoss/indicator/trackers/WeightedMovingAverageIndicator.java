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
 * Weighted moving average indicator.
 * 
 * @version 2017/08/25 23:51:34
 */
public class WeightedMovingAverageIndicator extends AbstractIndicator<Amount> {

    /**
     * 
     */
    private static final long serialVersionUID = 2563905432983128044L;

    private final Indicator<Amount> indicator;

    private final int tickSize;

    /**
     * @param indicator
     * @param tickSize
     */
    public WeightedMovingAverageIndicator(Indicator<Amount> indicator, int tickSize) {
        super(indicator.getTimeSeries());

        this.indicator = indicator;
        this.tickSize = tickSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        if (index == 0) {
            return indicator.getValue(0);
        }

        Amount value = Amount.ZERO;

        if (index - tickSize < 0) {
            for (int i = index + 1; i > 0; i--) {
                value = value.plus(Amount.of(i).multiply(indicator.getValue(i - 1)));
            }
            return value.divide(Amount.of(((index + 1) * (index + 2)) / 2));
        }

        int actualIndex = index;
        for (int i = tickSize; i > 0; i--) {
            value = value.plus(Amount.of(i).multiply(indicator.getValue(actualIndex)));
            actualIndex--;
        }
        return value.divide(Amount.of((tickSize * (tickSize + 1)) / 2));
    }
}
