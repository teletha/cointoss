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
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        // volume
        int volumeScale = market.service.setting.targetCurrencyMinimumBidSize.scale();

        bottom.line(Indicator.build(ticker, Tick::buyVolume).scale(volumeScale).ema(21), Long);
        bottom.line(Indicator.build(ticker, Tick::sellVolume).scale(volumeScale).ema(21), Short);

        Indicator<Num> buyPriceIncrease = Indicator.build(ticker, Tick::buyPriceIncrease).sma(14).scale(volumeScale);
        Indicator<Num> sellPriceDecrease = Indicator.build(ticker, Tick::sellPriceDecrease).sma(14).scale(volumeScale);
        bottomN.line(buyPriceIncrease.map(sellPriceDecrease, (b, s) -> b.minus(s)), Short);
    }
}
