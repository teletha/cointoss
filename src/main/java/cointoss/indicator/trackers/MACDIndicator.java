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
 * Moving average convergence divergence (MACDIndicator) indicator.
 * 
 * @version 2017/08/26 0:07:27
 */
public class MACDIndicator extends AbstractIndicator<Amount> {

    /**
     * 
     */
    private static final long serialVersionUID = 6986662450414841925L;

    private final ExponentialMovingAverageIndicator shortTerm;

    private final ExponentialMovingAverageIndicator longTerm;

    /**
     * @param indicator
     * @param shortTimeFrame
     * @param longTimeFrame
     */
    public MACDIndicator(Indicator<Amount> indicator, int shortTimeFrame, int longTimeFrame) {
        super(indicator);
        if (shortTimeFrame > longTimeFrame) {
            throw new IllegalArgumentException("Long term period count must be greater than short term period count");
        }

        shortTerm = new ExponentialMovingAverageIndicator(indicator, shortTimeFrame);
        longTerm = new ExponentialMovingAverageIndicator(indicator, longTimeFrame);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        return shortTerm.getValue(index).minus(longTerm.getValue(index));
    }
}
