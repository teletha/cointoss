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
import cointoss.ticker.Span;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
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

    public Style M1 = () -> {
        stroke.color(Color.rgb(107, 191, 71, 0.5));
    };

    public Style M10 = () -> {
        stroke.color(Color.rgb(17, 132, 66, 0.5));
    };

    public Style M30 = () -> {
        stroke.color(Color.rgb(57, 130, 195, 0.5));
    };

    public Style H1 = () -> {
        stroke.color(Color.rgb(157, 130, 195, 0.5));
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
            line(Indicators.waveTrend(market.tickers.on(Span.Minute1), channelLength.v, averageLength.v), M1);
            line(Indicators.waveTrend(market.tickers.on(Span.Minute10), channelLength.v, averageLength.v), M10);
            line(Indicators.waveTrend(market.tickers.on(Span.Minute30), channelLength.v, averageLength.v), M30);
            line(Indicators.waveTrend(market.tickers.on(Span.Hour1), channelLength.v, averageLength.v), H1);
        });
    }
}
