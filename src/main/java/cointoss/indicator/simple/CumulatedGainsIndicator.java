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
 * @version 2017/08/26 1:15:05
 */
public class CumulatedGainsIndicator extends AbstractPeriodicalDelegationIndicator {

    /**
     * 
     */
    private static final long serialVersionUID = 1164893749645777926L;

    /**
     * @param indicator
     * @param period
     */
    public CumulatedGainsIndicator(Indicator<Amount> indicator, int period) {
        super(indicator, period);

        calculator = index -> {
            Amount sumOfLosses = Amount.ZERO;

            for (int i = Math.max(1, index - period + 1); i <= index; i++) {
                Amount current = indicator.getValue(i);
                Amount prev = indicator.getValue(i - 1);

                if (current.isLessThan(prev)) {
                    sumOfLosses = sumOfLosses.plus(prev.minus(current));
                }
            }
            return sumOfLosses;
        };
    }

}
