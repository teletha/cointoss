/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Ticker;
import hypatia.Num;
import trademate.ChartTheme;
import trademate.chart.LineStyle;
import trademate.chart.PlotScript;

public class ATRIndicator extends PlotScript {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        Indicator<Num> atr = NumIndicator.averageTrueRange(ticker, 21).map(n -> n.scale(market.service.setting.base.scale));
        line(atr);

        Indicator<Num> tr = NumIndicator.trueRange(ticker).map(n -> n.scale(market.service.setting.base.scale));
        line(tr, new LineStyle(ChartTheme.$.buy));

        Indicator<Num> percentage = atr.map(tr, (avg, now) -> now.divide(avg).scale(3));
        line(percentage, new LineStyle(ChartTheme.$.sell));
    }
}