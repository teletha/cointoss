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
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotScript;

public class SMAIndicator extends PlotScript {

    public final Variable<Integer> shortDays = Variable.of(21);

    public final Variable<Integer> longDays = Variable.of(75);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        overlay.plot(Indicator.build(ticker, Tick::closePrice).sma(shortDays.v), S.CurrentSMA);
        overlay.plot(Indicator.build(market.tickers.of(Span.Minute30), Tick::closePrice).sma(shortDays.v), S.Minute30SMA);
        overlay.plot(Indicator.build(market.tickers.of(Span.Hour1), Tick::closePrice).sma(shortDays.v), S.Minute60SMA);
        overlay.plot(Indicator.build(market.tickers.of(Span.Hour4), Tick::closePrice).sma(shortDays.v), S.Minute240SMA);
        overlay.plot(Indicator.build(ticker, Tick::closePrice).sma(longDays.v), S.CurrentSMALong);
    }

    private interface S extends StyleDSL {

        double alpha = 0.5;

        Style CurrentSMA = () -> {
            stroke.color(Color.rgb(181, 212, 53, alpha));
        };

        Style Minute30SMA = () -> {
            stroke.color(Color.rgb(107, 191, 71, alpha));
        };

        Style Minute60SMA = () -> {
            stroke.color(Color.rgb(17, 132, 66, alpha));
        };

        Style Minute240SMA = () -> {
            stroke.color(Color.rgb(57, 130, 195, alpha));
        };

        Style CurrentSMALong = () -> {
            stroke.color(Color.rgb(54, 78, 161, alpha));
        };
    }
}
