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

import javafx.scene.text.TextFlow;

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import stylist.Style;
import trademate.chart.ChartCanvas.LineChart;

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
    final void plot(Market market, Ticker ticker, ChartView chart) {
        this.market = Objects.requireNonNull(market);
        this.ticker = Objects.requireNonNull(ticker);
        this.baseScale = market.service.setting.baseCurrencyScaleSize;
        this.targetScale = market.service.setting.targetCurrencyScaleSize;

        for (PlotDSL plotter : plotters) {
            plotter.lines.clear();
        }

        declare(market, ticker);

        for (PlotDSL plotter : plotters) {
            if (!plotter.lines.isEmpty()) {
                chart.infomations.getChildren().add(plotter.infomation);
                for (LineChart line : plotter.lines) {
                    if (line.infoText != null) {
                        plotter.infomation.getChildren().add(line.infoText);
                    }
                }
            }
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
    protected class PlotDSL {

        /** The associated {@link Indicator}s. */
        final List<LineChart> lines = new ArrayList();

        /** The plot area. */
        final PlotArea area;

        /** The infomation area. */
        final TextFlow infomation = new TextFlow();

        /** The bottom base position. */
        final double bottomUp;

        /** The max y-value. */
        double valueYMax = 0;

        /**
         * @param area
         */
        private PlotDSL(PlotArea area) {
            this.area = area;

            switch (area) {
            case Up:
                this.bottomUp = 100;
                break;

            case Overlay:
                this.bottomUp = 0;
                break;

            default:
                this.bottomUp = 0;
                break;
            }
        }

        /**
         * Calculate scale.
         * 
         * @return
         */
        double scale() {
            if (area != PlotArea.Overlay) {
                return 50 < valueYMax ? 50 / valueYMax : 1;
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
            line(Indicator.build(ticker, value), style);
        }
    }
}
