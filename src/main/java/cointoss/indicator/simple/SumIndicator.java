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
import cointoss.indicator.AbstractIndicator;
import cointoss.indicator.Indicator;

/**
 * @version 2017/08/25 22:47:25
 */
public class SumIndicator extends AbstractIndicator<Amount> {

    private static final long serialVersionUID = -7368429783906888789L;

    /** The accumlate size. */
    private final Indicator<Amount>[] indicators;

    /**
     * @param series
     * @param tickSize
     */
    public SumIndicator(Indicator<Amount>... indicators) {
        super(indicators[0].getTimeSeries());

        this.indicators = indicators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        Amount sum = Amount.ZERO;

        for (int i = 0; i < indicators.length; i++) {
            sum = sum.plus(indicators[i].getValue(index));
        }
        return sum;
    }
}
