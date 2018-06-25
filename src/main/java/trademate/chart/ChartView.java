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

import javafx.scene.layout.AnchorPane;

import cointoss.Market;
import cointoss.ticker.TickSpan;
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;

/**
 * @version 2018/06/26 1:19:42
 */
public class ChartView extends View {

    /** Chart UI */
    public @UI UIComboBox<TickSpan> chartSpan;

    /** Chart UI */
    public @UI UILabel selectDate;

    /** Chart UI */
    public @UI UILabel selectHigh;

    /** Chart UI */
    public @UI UILabel selectLow;

    /** Chart UI */
    public @UI AnchorPane chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    public void setMarket(Market market) {
        new CandleChart(chart, market).use(TickSpan.Second5);
    }
}
