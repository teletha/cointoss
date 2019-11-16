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

import javafx.scene.paint.Color;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import viewtify.util.FXUtils;

public abstract class PlotScript {

    /** The plotter. */
    protected final Plotter bottom = new Plotter(0);

    /** The plotter. */
    protected final Plotter up = new Plotter(1);

    /** The plotter. */
    protected final Plotter down = new Plotter(2);

    /** The plotter. */
    protected final Plotter top = new Plotter(3);

    /** The plotter. */
    protected final Plotter overlay = new Plotter(4);

    /** The all plotters. */
    final Plotter[] plotters = {bottom, up, down, top, overlay};

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
        for (Plotter plotter : plotters) {
            plotter.indicators.clear();
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
     * 
     */
    protected class Plotter {

        /** The associated {@link Indicator}s. */
        final List<IndicatorInfo> indicators = new ArrayList();

        /** The plot area. */
        final int area;

        /**
         * @param area
         */
        private Plotter(int area) {
            this.area = area;
        }

        /**
         * Plot the specified {@link Indicator}.
         * 
         * @param indicator A indicator to plot.
         */
        public final void plot(Indicator<? extends Number> indicator) {
            plot(indicator, null);
        }

        /**
         * Plot the specified {@link Indicator}.
         * 
         * @param indicator A indicator to plot.
         */
        public final void plot(Indicator<? extends Number> indicator, Style style) {
            if (style == null) {
                style = ChartStyles.MouseTrack;
            }
            indicators.add(new IndicatorInfo(indicator, style));
        }

        /**
         * Plot the specified value.
         * 
         * @param value A value to plot.
         */
        public final void plot(Number value) {
            plot(value, null);
        }

        /**
         * Plot the specified value.
         * 
         * @param value A value to plot.
         */
        public final void plot(Number value, Style style) {
            plot(Variable.of(value), style);
        }

        /**
         * Plot the specified value.
         * 
         * @param value A value to plot.
         */
        public final void plot(Variable<? extends Number> value) {
            plot(value, null);
        }

        /**
         * Plot the specified value.
         * 
         * @param value A value to plot.
         */
        public final void plot(Variable<? extends Number> value, Style style) {
            plot(Indicator.build(ticker, tick -> value.v));
        }
    }

    /**
     * Plotting indicator info holder.
     */
    static class IndicatorInfo {

        final Indicator<? extends Number> indicator;

        final Color color;

        /**
         * @param indicator
         * @param style
         */
        private IndicatorInfo(Indicator<? extends Number> indicator, Style style) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "stroke");
        }
    }
}
