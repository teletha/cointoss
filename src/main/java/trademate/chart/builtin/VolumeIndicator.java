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
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartStyles;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class VolumeIndicator extends PlotScript implements StyleDSL {

    public Style Long = () -> {
        stroke.color(ChartStyles.buy);
    };

    public Style Short = () -> {
        stroke.color(ChartStyles.sell);
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "Volume";
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

        in(PlotArea.Bottom, () -> {
            line(buyVolume.scale(volumeScale).name("Long"), Long);
            line(sellVolume.scale(volumeScale).name("Short"), Short);
        });
    }
}
