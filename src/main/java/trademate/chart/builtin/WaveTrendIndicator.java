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
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradeMateStyle;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;

public class WaveTrendIndicator extends PlotScript implements StyleDSL {

    public final Variable<Integer> channelLength = Variable.of(10);

    public final Variable<Integer> averageLength = Variable.of(21);

    public final Variable<Integer> overBoughtLevel1 = Variable.of(60);

    public final Variable<Integer> overSoldLevel1 = Variable.of(-60);

    public Style Main = () -> {
        stroke.color(TradeMateStyle.BUY).width(0.3, px);
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        NumIndicator indicator = Indicators.waveTrend(ticker, channelLength.v, averageLength.v);

        in(PlotArea.Low, () -> {
            line(0);
            line(overBoughtLevel1, Main);
            line(overSoldLevel1, Main);

            line(indicator, Main);
        });
    }
}
