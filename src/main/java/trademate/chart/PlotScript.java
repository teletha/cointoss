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
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;

public abstract class PlotScript {

    /** The current market. */
    private Market market;

    /** The current ticker. */
    private Ticker ticker;

    /** The associated {@link Indicator}s. */
    private List<Plotting> indicators = new ArrayList();

    /**
     * Execute plot declaration.
     * 
     * @param market
     * @param ticker
     */
    final void plot(Market market, Ticker ticker) {
        this.market = Objects.requireNonNull(market);
        this.ticker = Objects.requireNonNull(ticker);
        this.indicators.clear();

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
     * Plot the specified {@link Indicator}.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void plot(Indicator<? extends Number> indicator) {
        plot(indicator, null);
    }

    /**
     * Plot the specified {@link Indicator}.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void plot(Indicator<? extends Number> indicator, Style style) {
        indicators.add(new Plotting(indicator, style));
    }

    /**
     * Plot the specified value.
     * 
     * @param value A value to plot.
     */
    protected final void plot(Number value) {
        plot(value, null);
    }

    /**
     * Plot the specified value.
     * 
     * @param value A value to plot.
     */
    protected final void plot(Number value, Style style) {
        plot(Variable.of(value), style);
    }

    /**
     * Plot the specified value.
     * 
     * @param value A value to plot.
     */
    protected final void plot(Variable<? extends Number> value) {
        plot(value, null);
    }

    /**
     * Plot the specified value.
     * 
     * @param value A value to plot.
     */
    protected final void plot(Variable<? extends Number> value, Style style) {
        plot(Indicator.build(ticker, tick -> value.v));
    }

    /**
     * Plotting indicator info holder.
     */
    static class Plotting {

        final Indicator<? extends Number> indicator;

        final Style style;

        /**
         * @param indicator
         * @param style
         */
        private Plotting(Indicator<? extends Number> indicator, Style style) {
            this.indicator = indicator;
            this.style = style;
        }
    }
}
