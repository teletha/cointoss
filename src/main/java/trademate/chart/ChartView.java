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
import cointoss.util.Chrono;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.verify.BackTestView;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable candle date. */
    public final Variable<Ticker> ticker = Variable.of(Ticker.EMPTY);

    /** The candle type. */
    public final Variable<CandleType> candleType = Variable.of(CandleType.Price);

    /** Configuration UI */
    private UIButton config;

    /** Configuration UI */
    private UIComboBox<TimeSpan> span;

    /** Configuration UI */
    private UIComboBox<CandleType> candle;

    /** Configuration UI */
    public UICheckBox showLatestPrice;

    /** Chart UI */
    public Chart chart;

    /** The chart configuration. */
    public final Variable<Boolean> showOrderSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showPositionSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showRealtimeUpdate = Variable.of(true);

    /** The additional scripts. */
    public final ObservableList<Supplier<PlotScript>> scripts = FXCollections.observableArrayList();

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(sbox, style.chart, () -> {
                $(ui(chart));
                $(hbox, style.configBox, () -> {
                    $(span, style.span);
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
            display.width.fill().height.fill();
        };

        Style configBox = () -> {
            display.maxWidth(130, px).maxHeight(26, px);
            position.top(0, px).right(56, px);
        };

        Style span = () -> {
            font.size(11, px);
            display.minWidth(118, px);

            $.select(">.list-cell", () -> {
                text.align.center();
            });
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

        if (findAncestorView(BackTestView.class).isAbsent()) {
            span.renderSelectedWhen(v -> v.combineLatest(Chrono.seconds()).map(x -> {
                return Chrono.formatAsDuration(x.ⅱ, x.ⅰ.calculateNextStartTime(x.ⅱ)) + " / " + x.ⅰ;
            }));
        }

        config.text(FontAwesome.Glyph.GEAR).popup(new ViewDSL() {
            {
                $(vbox, () -> {
                    form("Candle Type", candle);
                    form("Latest Price", showLatestPrice);
                });
            }
        });
        candle.initialize(CandleType.values()).observing(candleType::set);
        showLatestPrice.initialize(true);
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
        showLatestPrice.value(state);
        showRealtimeUpdate.set(state);
    }
}
