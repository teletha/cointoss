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

        Indicator<Num> buyVolume = Indicator.build(ticker, Tick::buyVolume);
        Indicator<Num> sellVolume = Indicator.build(ticker, Tick::sellVolume);
        Indicator<Num> volumeDiff = buyVolume.map(sellVolume, (b, s) -> b.minus(s)).scale(market.service.setting.targetCurrencyScaleSize);
        Indicator<Boolean> down = Indicator.build(ticker, Tick::isBear).map(volumeDiff, (t, d) -> d.isNegative());
        Indicator<Boolean> up = Indicator.build(ticker, Tick::isBull).map(volumeDiff, (t, d) -> d.isPositive());

        bottom.line(buyVolume.ema(7).scale(volumeScale), Long);
        bottom.line(sellVolume.ema(7).scale(volumeScale), Short);

        main.mark(up, Long);
        main.mark(down, Short);
    }
}
