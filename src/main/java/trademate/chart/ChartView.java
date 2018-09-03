/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import cointoss.Market;
import cointoss.ticker.TickSpan;
import cointoss.ticker.Ticker;
import kiss.Variable;
import stylist.StyleDSL;
import viewtify.View;
import viewtify.dsl.Style;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                vbox(() -> {
                    hbox(() -> {
                        $(span);
                        $(selectDate);
                        $(selectHigh, S.Data);
                        $(selectLow, S.Data);
                        $(selectVolume, S.Data);
                        $(selectLongVolume, S.Data);
                        $(selectShortVolume, S.Data);
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
                .map(e -> e.ⅱ.tickers.tickerBy(e.ⅰ))
                .to(ticker::set);
    }

    /**
     * @version 2018/08/30 9:07:20
     */
    private static class S implements StyleDSL {

        static Style Data = () -> {
        };
    }
}
