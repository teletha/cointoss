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

import cointoss.Market;
import cointoss.ticker.TickSpan;
import cointoss.ticker.Ticker;
import kiss.I;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;
import viewtify.ui.View;

/**
 * @version 2018/08/30 9:37:10
 */
public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable cnadle date. */
    public final Variable<Ticker> ticker = Variable.of(Ticker.EMPTY);

    /** Chart UI */
    protected UIComboBox<TickSpan> span;

    /** Chart UI */
    protected UILabel selectDate;

    /** Chart UI */
    protected UILabel selectHigh;

    /** Chart UI */
    protected UILabel selectLow;

    /** Chart UI */
    protected UILabel selectVolume;

    /** Chart UI */
    protected UILabel selectLongVolume;

    /** Chart UI */
    protected UILabel selectShortVolume;

    /** The candle chart. */
    private final Chart chart = new Chart(this);

    /** The chart setting. */
    private final ChartDisplaySetting setting = I.make(ChartDisplaySetting.class);

    protected UICheckBox latest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, () -> {
                    $(hbox, () -> {
                        $(span);
                        $(selectDate, S.Data);
                        $(selectHigh, S.Data);
                        $(selectLow, S.Data);
                        $(selectVolume, S.Data);
                        $(selectLongVolume, S.Data);
                        $(selectShortVolume, S.Data);
                        $(latest, S.Data);
                    });
                    $(chart);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        span.values(0, TickSpan.class);

        span.observeNow() //
                .skipNull()
                .combineLatest(market.observeNow().skipNull())
                .map(e -> e.ⅱ.tickers.of(e.ⅰ))
                .to(ticker::set);

        latest.model(setting.showLatestPrice);
    }

    /**
     * @version 2018/09/08 20:10:52
     */
    private interface S extends StyleDSL {

        static Style Data = () -> {
            font.size(11, px);
            text.verticalAlign.middle();
            display.height(25, px).minWidth(50, px);
            padding.left(7, px);
        };
    }
}
