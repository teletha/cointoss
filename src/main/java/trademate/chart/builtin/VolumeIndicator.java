/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import trademate.CommonText;
import trademate.chart.ChartStyles;
import trademate.chart.LineStyle;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;
import viewtify.util.FXUtils;

public class VolumeIndicator extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        // volume
        int volumeScale = market.service.setting.target.minimumSize.scale();

        DoubleIndicator buyVolume = DoubleIndicator.build(ticker, Tick::longVolume);
        DoubleIndicator sellVolume = DoubleIndicator.build(ticker, Tick::shortVolume);

        in(PlotArea.Bottom, () -> {
            line(buyVolume.scale(volumeScale).name(CommonText.Buy), new LineStyle(FXUtils.color(ChartStyles.buy)));
            line(sellVolume.scale(volumeScale).name(CommonText.Sell), new LineStyle(FXUtils.color(ChartStyles.sell)));
        });
    }
}