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

import static cointoss.ticker.Span.*;

import javafx.scene.paint.Color;

import cointoss.Market;
import cointoss.ticker.DoubleIndicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import kiss.Variable;
import trademate.chart.LineStyle;
import trademate.chart.PlotScript;

public class SMAIndicator extends PlotScript {

    public final Variable<Integer> shortDays = Variable.of(21);

    public final Variable<Integer> longDays = Variable.of(200);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        int base = market.service.setting.base.scale;

        switch (ticker.span) {
        case Minute1:
        case Minute5:
            line(market.tickers.on(Minute5), shortDays, base, Color.hsb(0, 0.7, 0.8));

        case Minute15:
            line(market.tickers.on(Minute15), shortDays, base, Color.hsb(70, 0.7, 0.8));

        case Hour1:
            line(market.tickers.on(Hour1), shortDays, base, Color.hsb(140, 0.7, 0.8));

        case Hour4:
            line(market.tickers.on(Hour4), shortDays, base, Color.hsb(220, 0.7, 0.8));

        case Day:
            line(market.tickers.on(Day), longDays, base, Color.hsb(300, 0.7, 0.8));
        }
    }

    private void line(Ticker ticker, Variable<Integer> days, int base, Color style) {
        line(DoubleIndicator.build(ticker, Tick::closePrice).sma(days).scale(base).name(ticker.span.toString()), new LineStyle(style));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return "SMA(" + shortDays + ")";
    }
}