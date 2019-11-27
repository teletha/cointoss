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
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Span;
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
    protected PlotDSL bottom = new PlotDSL(PlotArea.Bottom, this);

    /** The plotter. */
    protected PlotDSL bottomN = new PlotDSL(PlotArea.BottomNarrow, this);

    /** The plotter. */
    protected PlotDSL low = new PlotDSL(PlotArea.Low, this);

    /** The plotter. */
    protected PlotDSL lowN = new PlotDSL(PlotArea.LowNarrow, this);

    /** The plotter. */
    protected PlotDSL high = new PlotDSL(PlotArea.High, this);

    /** The plotter. */
    protected PlotDSL highN = new PlotDSL(PlotArea.HighNarrow, this);

    /** The plotter. */
    protected PlotDSL top = new PlotDSL(PlotArea.Top, this);

    /** The plotter. */
    protected PlotDSL topN = new PlotDSL(PlotArea.TopNarrow, this);

    /** The plotter. */
    protected PlotDSL main = new PlotDSL(PlotArea.Main, this);

    private Cache<Span, PlotDSL[]> caches = CacheBuilder.newBuilder().maximumSize(6).build();

    /**
     * Execute plot declaration.
     * 
     * @param market
     * @param ticker
     */
    final Signal<PlotDSL> plot(Market market, Ticker ticker) {
        try {
            PlotDSL[] plotters = caches.get(ticker.span, () -> {
                bottom = new PlotDSL(PlotArea.Bottom, PlotScript.this);
                bottomN = new PlotDSL(PlotArea.BottomNarrow, PlotScript.this);
                low = new PlotDSL(PlotArea.Low, PlotScript.this);
                lowN = new PlotDSL(PlotArea.LowNarrow, PlotScript.this);
                high = new PlotDSL(PlotArea.High, PlotScript.this);
                highN = new PlotDSL(PlotArea.HighNarrow, PlotScript.this);
                top = new PlotDSL(PlotArea.Top, PlotScript.this);
                topN = new PlotDSL(PlotArea.TopNarrow, PlotScript.this);
                main = new PlotDSL(PlotArea.Main, PlotScript.this);

                declare(market, ticker);

                return new PlotDSL[] {bottom, bottomN, low, lowN, high, highN, top, topN, main};
            });

            return I.signal(plotters).skip(plotter -> plotter.lines.isEmpty());
        } catch (ExecutionException e) {
            throw I.quiet(e);
        }
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
    protected static class PlotDSL {

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

        /** The origin script. */
        final PlotScript origin;

        /**
         * @param area
         */
        private PlotDSL(PlotArea area, PlotScript origin) {
            this.area = area;
            this.origin = origin;
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
