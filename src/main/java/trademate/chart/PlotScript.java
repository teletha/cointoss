/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Tick;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import stylist.Style;
import trademate.chart.ChartCanvas.CandleStyle;
import trademate.chart.ChartCanvas.LineStyle;

public abstract class PlotScript {

    /** The plotter. */
    protected final PlotDSL bottom = new PlotDSL(PlotArea.Bottom);

    /** The plotter. */
    protected final PlotDSL up = new PlotDSL(PlotArea.Up);

    /** The plotter. */
    protected final PlotDSL down = new PlotDSL(PlotArea.Down);

    /** The plotter. */
    protected final PlotDSL top = new PlotDSL(PlotArea.Top);

    /** The plotter. */
    protected final PlotDSL overlay = new PlotDSL(PlotArea.Overlay);

    /** The base currency scale. */
    protected int baseScale;

    /** The base currency scale. */
    protected int targetScale;

    /** The all plotters. */
    final PlotDSL[] plotters = {bottom, up, down, top, overlay};

    /** The current market. */
    private Market market;

    /** The current ticker. */
    private Ticker ticker;

    /**
     * Execute plot declaration.
     * 
     * @param market
     * @param ticker
     */
    final void plot(Market market, Ticker ticker) {
        this.market = Objects.requireNonNull(market);
        this.ticker = Objects.requireNonNull(ticker);
        this.baseScale = market.service.setting.baseCurrencyScaleSize;
        this.targetScale = market.service.setting.targetCurrencyScaleSize;

        for (PlotDSL plotter : plotters) {
            plotter.lines.clear();
        }

        declare(market, ticker);
    }

    /**
     * Declare your chart.
     * 
     * @param market
     * @param ticker
     */
    protected abstract void declare(Market market, Ticker ticker);

    /**
     * Chart plotting DSL.
     */
    protected class PlotDSL {

        /** The associated {@link Indicator}s. */
        final List<LineStyle> lines = new ArrayList();

        /** The plot area. */
        final PlotArea area;

        /**
         * @param area
         */
        private PlotDSL(PlotArea area) {
            this.area = area;
        }

        /**
         * Plot the specified {@link Indicator} as line chart.
         * 
         * @param indicator A indicator to plot.
         */
        public final void line(Indicator<Num> indicator) {
            line(indicator, null);
        }

        /**
         * Plot the specified {@link Indicator} as line chart.
         * 
         * @param indicator A indicator to plot.
         */
        public final void line(Indicator<Num> indicator, Style style) {
            if (style == null) {
                style = ChartStyles.MouseTrack;
            }
            lines.add(new LineStyle(indicator, style));
        }

        /**
         * Plot the specified value as line chart.
         * 
         * @param value A value to plot.
         */
        public final void line(Number value) {
            line(value, null);
        }

        /**
         * Plot the specified value as line chart.
         * 
         * @param value A value to plot.
         */
        public final void line(Number value, Style style) {
            line(Num.of(value.toString()), style);
        }

        /**
         * Plot the specified value as line chart.
         * 
         * @param value A value to plot.
         */
        public final void line(Variable<? extends Number> value) {
            line(value, null);
        }

        /**
         * Plot the specified value as line chart.
         * 
         * @param value A value to plot.
         */
        public final void line(Variable<? extends Number> value, Style style) {
            line(value.v, style);
        }

        /**
         * Plot the specified value as line chart.
         * 
         * @param value A value to plot.
         */
        private final void line(Num value, Style style) {
            line(Indicator.build(ticker, value), style);
        }
    }
}
