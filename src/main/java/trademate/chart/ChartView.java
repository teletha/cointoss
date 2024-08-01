/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart;

import java.text.Normalizer.Form;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.Market;
import cointoss.ticker.Span;
import cointoss.ticker.Ticker;
import cointoss.util.Chrono;
import hypatia.Num;
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
                    $(config, style.config);
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
            display.maxWidth(152, px).maxHeight(26, px);
            position.top(0, px).right(54, px);
        };

        Style config = () -> {
            padding.horizontal(8, px).top(6, px).bottom(5, px);
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
        span.initialize(Span.Hour1, List.of(Span.values())).sort(Comparator.reverseOrder());
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
                $(vbox, FormStyles.Label90, FormStyles.LabelCenter, () -> {
                    form("Candle Type", FormStyles.Column3, showCandle, candleType);
                    form("Latest Price", FormStyles.Column3, showLatestPrice);
                    form("Orderbook", FormStyles.Column3, showOrderbook, orderbookPriceRange, orderbookHideSize);
                    form("Priced Volume", FormStyles.Column3, showPricedVolume, pricedVolumeType);
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
                .acceptPositiveDecimalInput()
                .normalizeInput(Form.NFKC)
                .maximumInput(6)
                .tooltip(en("Display a grouped board with a specified price range."))
                .enableWhen(showOrderbook.isSelected())
                .observing(e -> e.combineLatest(market.observe()), v -> {
                    v.ⅱ.orderBook.groupBy(v.ⅰ.floatValue());
                });

        pricedVolumeType.initialize(PriceRangedVolumeType.values()).enableWhen(showPricedVolume.isSelected());
    }

    public void loadPastData() {
        ZonedDateTime min = Chrono.utcBySeconds(chart.axisX.logicalMinValue.longValue());
        ZonedDateTime max = Chrono.utcBySeconds(chart.axisX.logicalMaxValue.longValue());

        if (ticker.isPresent()) {
            // market.v.tickers.add(market.v.service.log.range(min.minusDays(6), min.minusDays(1),
            // LogType.Fast)
            // .effect(e -> market.v.priceVolume.update(e)));
            // chart.layoutForcely();
        }
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