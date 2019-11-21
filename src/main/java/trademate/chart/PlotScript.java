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

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;
import stylist.Style;
import trademate.chart.ChartCanvas.Horizon;
import trademate.chart.ChartCanvas.LineChart;

public abstract class PlotScript {

    /** The plotter. */
    protected final PlotDSL bottom = new PlotDSL(PlotArea.Bottom);

    /** The plotter. */
    protected final PlotDSL low = new PlotDSL(PlotArea.Low);

    /** The plotter. */
    protected final PlotDSL lowN = new PlotDSL(PlotArea.LowNarrow);

    /** The plotter. */
    protected final PlotDSL high = new PlotDSL(PlotArea.High);

    /** The plotter. */
    protected final PlotDSL top = new PlotDSL(PlotArea.Top);

    /** The plotter. */
    protected final PlotDSL main = new PlotDSL(PlotArea.Main);

    /**
     * Execute plot declaration.
     * 
     * @param market
     * @param ticker
     */
    final Signal<PlotDSL> plot(Market market, Ticker ticker, ChartView chart) {
        PlotDSL[] plotters = {bottom, low, lowN, high, top, main};

        for (PlotDSL plotter : plotters) {
            plotter.lines.clear();
            plotter.horizons.clear();
        }

        declare(market, ticker);

        return I.signal(plotters).skip(plotter -> plotter.lines.isEmpty());
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
        final List<LineChart> lines = new ArrayList();

        /** The max y-value on line chart. */
        double lineMaxY = 0;

        /** The associated {@link Indicator}s. */
        final List<Horizon> horizons = new ArrayList();

        /** The max y-value on horizontal line. */
        private double horizonMaxY = 0;

        /** The plot area. */
        final PlotArea area;

        /**
         * @param area
         */
        private PlotDSL(PlotArea area) {
            this.area = area;
        }

        /**
         * Calculate scale.
         * 
         * @return
         */
        double scale() {
            double max = Math.max(horizonMaxY, lineMaxY);

            if (area != PlotArea.Main) {
                if (max < area.minHeight || area.maxHeight < max) {
                    return area.maxHeight / max;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
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
            lines.add(new LineChart(indicator, style));
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
            if (style == null) {
                style = ChartStyles.MouseTrack;
            }

            double v = value.doubleValue();
            if (horizonMaxY < v) {
                horizonMaxY = v;
            }

            horizons.add(new Horizon(v, style));
        }
    }
}
