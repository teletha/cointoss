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

import java.lang.reflect.Field;

import cointoss.Market;
import cointoss.Trader;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.Plot;
import trademate.chart.PlotScript;

public class TraderPlot extends PlotScript implements StyleDSL {

    private final Trader trader;

    /**
     * @param trader
     */
    public TraderPlot(Trader trader) {
        this.trader = trader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, Ticker ticker) {
        for (Field field : trader.getClass().getDeclaredFields()) {
            Plot plot = field.getAnnotation(Plot.class);

            if (plot != null && Indicator.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    Indicator indicator = (Indicator) field.get(trader);
                    Style style = () -> {
                        stroke.color(plot.color());
                    };
                    PlotDSL dsl;
                    switch (plot.area()) {
                    case Bottom:
                        dsl = bottom;
                        break;
                    case BottomNarrow:
                        dsl = bottomN;
                        break;
                    case High:
                        dsl = high;
                        break;
                    case HighNarrow:
                        dsl = highN;
                        break;
                    case Low:
                        dsl = low;
                        break;
                    case LowNarrow:
                        dsl = lowN;
                        break;
                    case Top:
                        dsl = top;
                        break;
                    case TopNarrow:
                        dsl = topN;
                        break;
                    default:
                        dsl = main;
                        break;
                    }
                    dsl.line(indicator, style);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
