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
import cointoss.ticker.Indicators;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import kiss.Ⅱ;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradeMateStyle;
import trademate.chart.PlotScript;

public class WaveTrendIndicator extends PlotScript implements StyleDSL {

    public final Variable<Integer> channelLength = Variable.of(10);

    public final Variable<Integer> averageLength = Variable.of(21);

    public final Variable<Integer> overBoughtLevel1 = Variable.of(60);

    public final Variable<Integer> overBoughtLevel2 = Variable.of(53);

    public final Variable<Integer> overSoldLevel1 = Variable.of(-60);

    public final Variable<Integer> overSoldLevel2 = Variable.of(-53);

    public Style Main = () -> {
        stroke.color(TradeMateStyle.BUY).width(0.3, px);
    };

    public Style Lazy = () -> {
        stroke.color(TradeMateStyle.SELL).width(0.3, px);
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        Indicator<Ⅱ<Num, Num>> indicator = Indicators.waveTrend(ticker, channelLength.v, averageLength.v);

        up.line(0);
        up.line(overBoughtLevel1, Lazy);
        up.line(overBoughtLevel2, Lazy);
        up.line(overSoldLevel1, Main);
        up.line(overSoldLevel2, Main);

        up.line(indicator.map(Ⅱ::ⅰ), Main);
        up.line(indicator.map(Ⅱ::ⅱ), Lazy);
    }
}
