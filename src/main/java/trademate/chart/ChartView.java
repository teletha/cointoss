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
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;

/**
 * @version 2018/06/26 10:14:03
 */
public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The list of plottable cnadle date. */
    public final Variable<Ticker> ticker = Variable.of(Ticker.EMPTY);

    /** Chart UI */
    protected @UI UIComboBox<TickSpan> span;

    /** Chart UI */
    protected @UI UILabel selectDate;

    /** Chart UI */
    protected @UI UILabel selectHigh;

    /** Chart UI */
    protected @UI UILabel selectLow;

    /** The candle chart. */
    private final @UI Chart chart = new Chart(this);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        span.values(0, TickSpan.class);

        span.observeNow().skipNull().combineLatest(market.observeNow().skipNull()).map(e -> e.ⅱ.tickerBy(e.ⅰ)).to(ticker::set);
    }
}
