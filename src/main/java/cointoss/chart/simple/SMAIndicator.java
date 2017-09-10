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

import cointoss.chart.ComposableIndicator;
import cointoss.chart.Indicator;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/10 14:12:54
 */
public class SMAIndicator extends ComposableIndicator {

    private final int timeFrame;

    /**
     * @param chart
     */
    public SMAIndicator(Indicator<Decimal> indicator, int timeFrame) {
        super(indicator);

        this.timeFrame = timeFrame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Decimal calculate(int index) {
        Decimal sum = Decimal.ZERO;

        for (int i = Math.max(0, index - timeFrame + 1); i <= index; i++) {
            sum = sum.plus(indicator.get(i));
        }

        final int realTimeFrame = Math.min(timeFrame, index + 1);
        return sum.dividedBy(Decimal.valueOf(realTimeFrame));
    }
}
