/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.builtin;

import static cointoss.ticker.Span.*;

import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import stylist.value.Color;
import trademate.chart.PlotScript;

public class SMAIndicator extends PlotScript {

    public final Variable<Integer> shortDays = Variable.of(21);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        int base = market.service.setting.base.scale;

        line(market.tickers.on(Minute5), shortDays, base, style.SMA5M);
        line(market.tickers.on(Minute15), shortDays, base, style.SMA15M);
        line(market.tickers.on(Hour1), shortDays, base, style.SMA1H);
        line(market.tickers.on(Hour4), shortDays, base, style.SMA4H);
    }

    private void line(Ticker ticker, Variable<Integer> days, int base, Style style) {
        line(DoubleIndicator.build(ticker, Tick::closePrice).sma(days).scale(base).name(ticker.span.toString()), style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return "SMA(" + shortDays + ")";
    }

    /**
     * 
     */
    interface style extends StyleDSL {
        double alpha = 0.7;

        Style SMA5M = () -> {
            stroke.color(Color.rgb(207, 89, 71, alpha));
        };

        Style SMA15M = () -> {
            stroke.color(Color.rgb(101, 89, 71, alpha));
        };

        Style SMA30M = () -> {
            stroke.color(Color.rgb(107, 191, 71, alpha));
        };

        Style SMA1H = () -> {
            stroke.color(Color.rgb(17, 132, 66, alpha));
        };

        Style SMA4H = () -> {
            stroke.color(Color.rgb(57, 80, 195, alpha));
        };
    }
}