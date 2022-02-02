/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.text.Normalizer.Form;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.verify.BackTestView;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable candle date. */
    public final Variable<Ticker> ticker = Variable.empty();

    /** Configuration UI */
    private UIButton config;

    /** Configuration UI */
    private UIComboBox<Span> span;

    /** Configuration UI */
    public UIComboBox<CandleType> candleType;

    /** Configuration UI */
    public UICheckBox showCandle;

    /** Configuration UI */
    public UICheckBox showLatestPrice;

    /** Configuration UI */
    public UICheckBox showOrderbook;

    /** Configuration UI */
    public UIText<Num> orderbookPriceRange;

    /** Configuration UI */
    public UISpinner<Integer> orderbookHideSize;

    /** Configuration UI */
    public UICheckBox showPricedVolume;

    /** Configuration UI */
    public UIComboBox<PriceRangedVolumeType> pricedVolumeType;

    /** Chart UI */
    public Chart chart;

    /** The chart configuration. */
    public final Variable<Boolean> showOrderSupport = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showRealtimeUpdate = Variable.of(false);

    /** The chart configuration. */
    public final Variable<Boolean> showChart = Variable.of(true);

    /** The chart configuration. */
    public final Variable<Boolean> showIndicator = Variable.of(true);

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
            position.top(0, px).right(54, px);
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
        span.initialize(Span.values()).sort(Comparator.reverseOrder());
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
                    form("Candle Type", showCandle, candleType);
                    form("Latest Price", showLatestPrice);
                    form("Orderbook", FormStyles.FormInputMin, showOrderbook, orderbookPriceRange, orderbookHideSize);
                    form("Priced Volume", FormStyles.FormInputMin, showPricedVolume, pricedVolumeType);
                });
            }
        });
        candleType.initialize(CandleType.values());
        showCandle.initialize(true);
        showLatestPrice.initialize(true);
        showOrderbook.initialize(true);
        showPricedVolume.initialize(true);

        orderbookHideSize.initialize(IntStream.range(0, 101))
                .tooltip(en("Display only boards that are larger than the specified size."))
                .enableWhen(showOrderbook.isSelected());
        orderbookPriceRange.initializeLazy(market.observe().map(m -> m.service.setting.base.minimumSize))
                .acceptPositiveNumberInput()
                .normalizeInput(Form.NFKC)
                .maximumInput(6)
                .tooltip(en("Display a grouped board with a specified price range."))
                .enableWhen(showOrderbook.isSelected())
                .observing(e -> e.combineLatest(market.observe()), v -> {
                    v.ⅱ.orderBook.longs.groupBy(v.ⅰ);
                    v.ⅱ.orderBook.shorts.groupBy(v.ⅰ);
                });

        pricedVolumeType.initialize(PriceRangedVolumeType.values()).enableWhen(showPricedVolume.isSelected());
    }

    /**
     * 
     */
    private static class ChartConfig extends View {

        /**
         * {@inheritDoc}
         */
        @Override
        protected ViewDSL declareUI() {
            return new ViewDSL() {
                {

                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
        }
    }
}