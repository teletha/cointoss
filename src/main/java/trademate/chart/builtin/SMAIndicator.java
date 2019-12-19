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
import cointoss.ticker.NumIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotScript;

public class SMAIndicator extends PlotScript implements StyleDSL {

    public final Variable<Integer> shortDays = Variable.of(21);

    public final Variable<Integer> longDays = Variable.of(75);

    private double alpha = 0.5;

    public Style shortSMA = () -> {
        stroke.color(Color.rgb(181, 212, 53, alpha));
    };

    public Style longSMA = () -> {
        stroke.color(Color.rgb(54, 78, 161, alpha));
    };

    public Style SMA30M = () -> {
        stroke.color(Color.rgb(107, 191, 71, alpha));
    };

    public Style SMA1H = () -> {
        stroke.color(Color.rgb(17, 132, 66, alpha));
    };

    public Style SMA4H = () -> {
        stroke.color(Color.rgb(57, 130, 195, alpha));
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        int base = market.service.setting.baseCurrencyScaleSize;

        line(NumIndicator.build(ticker, Tick::closePrice).sma(shortDays).scale(base), shortSMA);
        line(NumIndicator.build(market.tickers.of(TimeSpan.Minute30), Tick::closePrice).sma(shortDays).scale(base), SMA30M);
        line(NumIndicator.build(market.tickers.of(TimeSpan.Hour1), Tick::closePrice).sma(shortDays).scale(base), SMA1H);
        line(NumIndicator.build(market.tickers.of(TimeSpan.Hour4), Tick::closePrice).sma(shortDays).scale(base), SMA4H);
        line(NumIndicator.build(ticker, Tick::closePrice).sma(longDays).scale(base), longSMA);
    }
}
