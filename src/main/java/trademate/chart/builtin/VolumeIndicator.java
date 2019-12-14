/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.builtin;

import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartStyles;
import trademate.chart.PlotScript;

public class VolumeIndicator extends PlotScript implements StyleDSL {

    public Style Long = () -> {
        stroke.color(ChartStyles.buy);
    };

    public Style Short = () -> {
        stroke.color(ChartStyles.sell);
    };

    public final Variable<Integer> emaLength = Variable.of(1);

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return super.name() + "(" + emaLength + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        // volume
        int volumeScale = market.service.setting.targetCurrencyMinimumBidSize.scale();

        DoubleIndicator buyVolume = DoubleIndicator.build(ticker, Tick::buyVolume);
        DoubleIndicator sellVolume = DoubleIndicator.build(ticker, Tick::sellVolume);

        bottom.line(buyVolume.ema(emaLength.v).scale(volumeScale), Long);
        bottom.line(sellVolume.ema(emaLength.v).scale(volumeScale), Short);
    }
}
