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

import java.util.Comparator;
import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.Market;
import cointoss.ticker.Ticker;
import cointoss.ticker.TimeSpan;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;

public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable candle date. */
    public final Variable<Ticker> ticker = Variable.of(Ticker.EMPTY);

    /** The candle type. */
    public final Variable<CandleType> candleType = Variable.of(CandleType.Price);

    /** Chart UI */
    protected UIComboBox<TimeSpan> span;

    /** Chart UI */
    protected UIButton config;

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
    public final Chart chart = new Chart(this);

    /**
     * UI definition.
     */
    class view extends UI {
        {
            $(sbox, style.chart, () -> {
                $(chart);
                $(hbox, style.configBox, () -> {
                    $(span);
                    $(config);
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
            display.maxWidth(130, px).maxHeight(26, px);
            position.top(0, px).right(56, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        span.initialize(TimeSpan.values()).sort(Comparator.reverseOrder());
        span.observing() //
                .skipNull()
                .combineLatest(market.observing().skipNull())
                .map(e -> e.ⅱ.tickers.on(e.ⅰ))
                .to(ticker::set);

        config.text(FontAwesome.Glyph.GEAR).popover(new Config());
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

    /**
     * 
     */
    class Config extends View {

        private UIComboBox<CandleType> candle;

        class view extends UI {
            {
                $(vbox, () -> {
                    form("Candle Type", candle);
                });
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            candle.initialize(CandleType.values()).observe(candleType::set);
        }
    }
}
