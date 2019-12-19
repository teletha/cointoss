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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cointoss.Market;
import cointoss.ticker.AbstractIndicator;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartCanvas.CandleMark;
import trademate.chart.ChartCanvas.Horizon;
import trademate.chart.ChartCanvas.LineChart;

public abstract class PlotScript implements StyleDSL {

    /** The curretn plotting area. */
    private PlotArea area = PlotArea.Main;

    /** The declared plotters. */
    final Map<PlotArea, PlotDSL> plotters = new HashMap();

    /**
     * Specify the contextual plot area.
     * 
     * @param area
     * @param declare
     */
    protected final void in(PlotArea area, Runnable declare) {
        PlotArea prev = this.area;
        this.area = area;
        declare.run();
        this.area = prev;
    }

    /**
     * Plot the specified {@link Indicator} as line chart.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void line(AbstractIndicator<? extends Number, ?> indicator) {
        line(indicator, null);
    }

    /**
     * Plot the specified {@link Indicator} as line chart.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void line(AbstractIndicator<? extends Number, ?> indicator, Style style) {
        line(indicator, style, null);
    }

    /**
     * Plot the specified {@link Indicator} as line chart.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void line(AbstractIndicator<? extends Number, ?> indicator, Style style, Indicator<String> info) {
        if (style == null) {
            style = ChartStyles.MouseTrack;
        }
        PlotDSL plotter = plotters.computeIfAbsent(area, k -> new PlotDSL(k, this));
        plotter.lines.add(new LineChart(indicator.memoize(), style, info));
    }

    /**
     * Plot the specified value as line chart.
     * 
     * @param value A value to plot.
     */
    protected final void line(Number value) {
        line(value, null);
    }

    /**
     * Plot the specified value as line chart.
     * 
     * @param value A value to plot.
     */
    protected final void line(Number value, Style style) {
        line(Num.of(value.toString()), style);
    }

    /**
     * Plot the specified value as line chart.
     * 
     * @param value A value to plot.
     */
    protected final void line(Variable<? extends Number> value) {
        line(value, null);
    }

    /**
     * Plot the specified value as line chart.
     * 
     * @param value A value to plot.
     */
    protected final void line(Variable<? extends Number> value, Style style) {
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

        PlotDSL plotter = plotters.computeIfAbsent(area, k -> new PlotDSL(k, this));

        double v = value.doubleValue();
        if (plotter.horizonMaxY < v) {
            plotter.horizonMaxY = v;
        }

        plotter.horizons.add(new Horizon(v, style));
    }

    /**
     * Plot the specified {@link Indicator} as mark.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void mark(Indicator<Boolean> indicator) {
        mark(indicator, null);
    }

    /**
     * Plot the specified {@link Indicator} as mark.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void mark(Indicator<Boolean> indicator, Style style) {
        if (style == null) {
            style = ChartStyles.MouseTrack;
        }

        PlotDSL plotter = plotters.computeIfAbsent(area, k -> new PlotDSL(k, this));
        plotter.candles.add(new CandleMark(indicator, style));
    }

    /**
     * Declare your chart.
     * 
     * @param market
     * @param ticker
     */
    protected abstract void declare(Market market, Ticker ticker);

    /**
     * Return the indicator name.
     * 
     * @return
     */
    public String name() {
        Class clazz = getClass();
        if (clazz.isMemberClass() || clazz.isAnonymousClass() || clazz.isLocalClass()) {
            clazz = clazz.getEnclosingClass();
        }
        return clazz.getSimpleName().replace("Indicator", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name();
    }

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

        /** The associated {@link Indicator}s. */
        final List<CandleMark> candles = new ArrayList();

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
    }
}
