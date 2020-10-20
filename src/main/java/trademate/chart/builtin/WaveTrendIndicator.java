/*
 * Copyright (C) 2020 cointoss Development Team
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
import cointoss.ticker.Indicators;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.Theme;
import trademate.chart.PlotArea;
import trademate.chart.PlotScript;
import viewtify.util.FXUtils;

public class WaveTrendIndicator extends PlotScript {

    public final Variable<Integer> channelLength = Variable.of(10);

    public final Variable<Integer> averageLength = Variable.of(21);

    public final Variable<Integer> overBoughtLevel1 = Variable.of(60);

    public final Variable<Integer> overSoldLevel1 = Variable.of(-60);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        DoubleIndicator indicator = Indicators.waveTrend(ticker, channelLength.v, averageLength.v);

        in(PlotArea.Low, () -> {
            line(0);
            line(overBoughtLevel1, style.Main);
            line(overSoldLevel1, style.Main);

            line(indicator, style.Main);
            line(Indicators.waveTrend(market.tickers.on(Span.Minute15), channelLength.v, averageLength.v), style.M10);
            line(Indicators.waveTrend(market.tickers.on(Span.Minute30), channelLength.v, averageLength.v), style.M30);
            line(Indicators.waveTrend(market.tickers.on(Span.Hour1), channelLength.v, averageLength.v), style.H1);
        });
    }

    interface style extends StyleDSL {
        Style Main = () -> {
            stroke.color(FXUtils.color(Theme.$.Long.v)).width(0.3, px);
        };

        Style M10 = () -> {
            stroke.color(Color.rgb(17, 132, 66, 0.5));
        };

        Style M30 = () -> {
            stroke.color(Color.rgb(57, 130, 195, 0.5));
        };

        Style H1 = () -> {
            stroke.color(Color.rgb(157, 130, 195, 0.5));
        };
    }
}