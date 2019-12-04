/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.ui.UI;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;

public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable cnadle date. */
    public final Variable<Ticker> ticker = Variable.of(Ticker.EMPTY);

    /** Chart UI */
    protected UIComboBox<Span> span;

    /** The chart configuration. */
    public final Variable<Boolean> showLatestPrice = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showOrderSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showPositionSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showRealtimeUpdate = Variable.of(true);

    /** The additional scripts. */
    public final ObservableList<Supplier<PlotScript>> scripts = FXCollections.observableArrayList();

    /** The candle chart. */
    private final Chart chart = new Chart(this);

    /**
     * UI definition.
     */
    class view extends UI {
        {
            $(sbox, style.chart, () -> {
                $(chart);
                $(hbox, style.configBox, () -> {
                    $(span);
                });
            });
        }
    }

    /**
     * Style definition.
     */
    interface style extends StyleDSL {
        Style chart = () -> {
            display.width.fill();
        };

        Style configBox = () -> {
            display.maxWidth(100, px).maxHeight(10, px);
            position.top(0, px).right(56, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        span.items(Span.class).initial(Span.Minute1);
        span.observeNow() //
                .skipNull()
                .combineLatest(market.observeNow().skipNull())
                .map(e -> e.ⅱ.tickers.of(e.ⅰ))
                .to(ticker::set);
    }

    /**
     * Restore realtime UI updating.
     */
    public void restoreRealtimeUpdate() {
        realtimeUpdate(true);
    }

    /**
     * Reduce realtime UI updating.
     */
    public void reduceRealtimeUpdate() {
        realtimeUpdate(false);
    }

    /**
     * Change realtime UI updating strategy.
     * 
     * @param state
     */
    private void realtimeUpdate(boolean state) {
        showOrderSupport.set(state);
        showPositionSupport.set(state);
        showLatestPrice.set(state);
        showRealtimeUpdate.set(state);
    }
}
