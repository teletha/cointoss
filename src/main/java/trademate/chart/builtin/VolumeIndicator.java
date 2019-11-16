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
import trademate.chart.ChartStyles;
import trademate.chart.PlotScript;

public class VolumeIndicator extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        bottom.plot(Indicator.build(ticker, Tick::buyVolume), ChartStyles.OrderSupportBuy);
        bottom.plot(Indicator.build(ticker, Tick::sellVolume), ChartStyles.OrderSupportSell);
    }
}
