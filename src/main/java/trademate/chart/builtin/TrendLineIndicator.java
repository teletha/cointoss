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
import cointoss.ticker.Indicators;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotScript;

public class TrendLineIndicator extends PlotScript implements StyleDSL {

    public final Variable<Integer> tickLength = Variable.of(50);

    private double alpha = 0.5;

    public Style SupportLine = () -> {
        stroke.color(Color.rgb(53, 53, 223, alpha));
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        line(Indicators.trendLine(ticker, tickLength.v), SupportLine);
    }
}
