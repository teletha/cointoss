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
import cointoss.util.Num;
import kiss.Variable;
import stylist.Style;
import viewtify.util.FXUtils;

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
            plotter.styles.clear();
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
        final List<PlotStyle> styles = new ArrayList();

        /** The plot area. */
        final PlotArea area;

        /**
         * @param area
         */
        private PlotDSL(PlotArea area) {
            this.area = area;
        }

        /**
         * Plot the specified {@link Indicator}.
         * 
         * @param indicator A indicator to plot.
         */
        public final void plot(Indicator<Num> indicator) {
            plot(indicator, null);
        }

        /**
         * Plot the specified {@link Indicator}.
         * 
         * @param indicator A indicator to plot.
         */
        public final void plot(Indicator<Num> indicator, Style style) {
            if (style == null) {
                style = ChartStyles.MouseTrack;
            }
            styles.add(new PlotStyle(indicator, style));
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
            plot(Num.of(value.toString()), style);
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
            plot(value.v, style);
        }

        /**
         * Plot the specified value.
         * 
         * @param value A value to plot.
         */
        private final void plot(Num value, Style style) {
            plot(Indicator.build(ticker, value), style);
        }
    }

    /**
     * 
     */
    static class PlotStyle {

        /** The indicator. */
        final Indicator<? extends Number> indicator;

        /** The indicator color. */
        final Color color;

        /** The indicator line width. */
        final double width;

        /** The indicator line style. */
        final double[] dashArray;

        /**
         * @param indicator
         * @param style
         */
        private PlotStyle(Indicator<? extends Number> indicator, Style style) {
            this.indicator = indicator;
            this.color = FXUtils.color(style, "stroke");
            this.width = FXUtils.length(style, "stroke-width");
            this.dashArray = FXUtils.lengths(style, "stroke-dasharray");
        }
    }
}
