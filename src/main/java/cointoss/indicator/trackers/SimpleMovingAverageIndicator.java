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
 * @version 2017/08/25 23:51:34
 */
public class SimpleMovingAverageIndicator extends AbstractIndicator<Amount> {

    /**
     * 
     */
    private static final long serialVersionUID = 2576322826449589546L;

    private final Indicator<Amount> indicator;

    private final int tickSize;

    /**
     * @param indicator
     * @param tickSize
     */
    public SimpleMovingAverageIndicator(Indicator<Amount> indicator, int tickSize) {
        super(indicator.getTimeSeries());

        this.indicator = indicator;
        this.tickSize = tickSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        Amount sum = Amount.ZERO;

        for (int i = Math.max(0, index - tickSize + 1); i <= index; i++) {
            sum = sum.plus(indicator.getValue(i));
        }
        return sum.divide(Amount.of(Math.min(tickSize, index + 1)));
    }

}
