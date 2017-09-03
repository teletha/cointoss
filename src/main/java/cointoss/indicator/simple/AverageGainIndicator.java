/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.indicator.simple;

import cointoss.Amount;
import cointoss.indicator.Indicator;

/**
 * @version 2017/08/26 1:30:10
 */
public class AverageGainIndicator extends AbstractPeriodicalDelegationIndicator {

    /**
     * 
     */
    private static final long serialVersionUID = 294212118465738037L;

    /**
     * @param indicator
     * @param period
     */
    public AverageGainIndicator(Indicator<Amount> indicator, int period) {
        super(new CumulatedGainsIndicator(indicator, period), period);

        calculator = index -> {
            return delegator.getValue(index).divide(Amount.of(Math.min(period, index + 1)));
        };
    }
}
