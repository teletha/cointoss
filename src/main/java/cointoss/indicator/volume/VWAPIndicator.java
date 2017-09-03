/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.indicator.volume;

import cointoss.Amount;
import cointoss.TimeSeries;
import cointoss.indicator.AbstractIndicator;
import cointoss.indicator.Indicator;
import cointoss.indicator.simple.TypicalPriceIndicator;
import cointoss.indicator.simple.VolumeIndicator;

/**
 * The volume-weighted average price (VWAP) Indicator.
 * 
 * @see http://www.investopedia.com/articles/trading/11/trading-with-vwap-mvwap.asp
 * @see http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:vwap_intraday
 * @see https://en.wikipedia.org/wiki/Volume-weighted_average_price
 * @version 2017/08/25 22:47:25
 */
public class VWAPIndicator extends AbstractIndicator<Amount> {

    /**
     * 
     */
    private static final long serialVersionUID = 2519208026918309590L;

    /** The accumlate size. */
    private final int tickSize;

    private final Indicator<Amount> typical;

    private final Indicator<Amount> volume;

    /**
     * @param series
     * @param tickSize
     */
    public VWAPIndicator(TimeSeries series, int tickSize) {
        super(series);

        this.tickSize = tickSize <= 0 ? 1 : tickSize;
        typical = new TypicalPriceIndicator(series);
        volume = new VolumeIndicator(series, tickSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Amount calculate(int index) {
        if (index <= 0) {
            return typical.getValue(index);
        }

        Amount cumulativeTPV = Amount.ZERO;
        Amount cumulativeVolume = Amount.ZERO;

        for (int i = Math.max(0, index - tickSize + 1); i <= index; i++) {
            Amount currentVolume = volume.getValue(i);
            cumulativeTPV = cumulativeTPV.plus(typical.getValue(i).multiply(currentVolume));
            cumulativeVolume = cumulativeVolume.plus(currentVolume);
        }
        return cumulativeTPV.divide(cumulativeVolume);
    }
}
