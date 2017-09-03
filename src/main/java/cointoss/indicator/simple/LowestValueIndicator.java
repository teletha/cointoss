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
 * @version 2017/08/25 22:47:25
 */
public class LowestValueIndicator extends AbstractPeriodicalDelegationIndicator {

    /**
     * 
     */
    private static final long serialVersionUID = -98503231091472463L;

    /**
     * @param indicator
     * @param tickSize
     */
    public LowestValueIndicator(Indicator<Amount> indicator, int tickSize) {
        super(indicator, tickSize);

        calculator = index -> {
            int start = Math.max(0, index - tickSize + 1);
            Amount lowest = indicator.getValue(start);

            for (int i = start + 1; i <= index; i++) {
                lowest = Amount.min(lowest, indicator.getValue(i));
            }
            return lowest;
        };
    }
}
