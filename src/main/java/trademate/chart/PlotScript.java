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

import cointoss.Market;
import cointoss.ticker.Indicator;
import cointoss.ticker.Ticker;
import cointoss.util.Num;
import kiss.Variable;
import stylist.Style;

public abstract class PlotScript {

    /**
     * Plot your chart.
     * 
     * @param market
     * @param ticker
     */
    protected abstract void plot(Market market, Ticker ticker);

    /**
     * Plot the specified {@link Indicator}.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void plot(Indicator<? extends Num> indicator) {
        plot(indicator, null);
    }

    /**
     * Plot the specified {@link Indicator}.
     * 
     * @param indicator A indicator to plot.
     */
    protected final void plot(Indicator<? extends Num> indicator, Style style) {

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

    }
}
