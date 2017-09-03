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
import cointoss.indicator.Indicator;
import cointoss.indicator.simple.AbstractPeriodicalDelegationIndicator;
import cointoss.indicator.simple.AverageGainIndicator;
import cointoss.indicator.simple.AverageLossIndicator;

/**
 * Relative strength index indicator.
 * <p>
 * This calculation of RSI uses traditional moving averages as opposed to Wilder's accumulative
 * moving average technique.
 * <p>
 * See reference
 * <a href="https://www.barchart.com/education/technical-indicators#/studies/std_rsi_mod"> RSI
 * calculation</a>.
 * 
 * @version 2017/08/26 2:39:01
 */
public class RelativeStrengthIndexIndicator extends AbstractPeriodicalDelegationIndicator {

    /**
     * 
     */
    private static final long serialVersionUID = 866393014398422023L;

    private Indicator<Amount> averageGainIndicator;

    private Indicator<Amount> averageLossIndicator;

    /**
     * @param indicator
     * @param period
     */
    private RelativeStrengthIndexIndicator(Indicator<Amount> indicator, int period) {
        super(indicator, period);

        averageGainIndicator = new AverageGainIndicator(indicator, period);
        averageLossIndicator = new AverageLossIndicator(indicator, period);

        calculator = index -> {
            if (index == 0) {
                return Amount.ZERO;
            }

            // Relative strength
            Amount averageLoss = averageLossIndicator.getValue(index);
            if (averageLoss.isZero()) {
                return Amount.HUNDRED;
            }
            Amount averageGain = averageGainIndicator.getValue(index);
            Amount relativeStrength = averageGain.divide(averageLoss);

            // Nominal case
            Amount ratio = Amount.HUNDRED.divide(Amount.ONE.plus(relativeStrength));
            return Amount.HUNDRED.minus(ratio);
        };
    }
}
