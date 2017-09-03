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
import cointoss.TimeSeries;
import cointoss.indicator.AbstractIndicator;

/**
 * @version 2017/08/25 22:47:25
 */
public class VolumeIndicator extends AbstractIndicator<Amount> {

    private static final long serialVersionUID = -7368429783906888789L;

    /** The accumlate size. */
    private final int tickSize;

    /**
     * @param series
     * @param tickSize
     */
    public VolumeIndicator(TimeSeries series, int tickSize) {
        super(series);

        this.tickSize = tickSize <= 0 ? 1 : tickSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        Amount sum = Amount.ZERO;

        for (int i = Math.max(0, index - tickSize + 1); i <= index; i++) {
            sum = sum.plus(series.getTick(i).volume);
        }
        return sum;
    }
}
