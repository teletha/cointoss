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

import javafx.scene.layout.Pane;

import cointoss.Market;
import cointoss.ticker.TickSpan;
import kiss.Variable;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;

/**
 * @version 2018/06/26 10:14:03
 */
public class ChartView extends View {

    /** The associated market. */
    public final Variable<Market> market = Variable.empty();

    /** The candle chart UI. */
    public final CandleChart candle = new CandleChart(this);

    /** Chart UI */
    protected @UI UIComboBox<TickSpan> chartSpan;

    /** Chart UI */
    protected @UI UILabel selectDate;

    /** Chart UI */
    protected @UI UILabel selectHigh;

    /** Chart UI */
    protected @UI UILabel selectLow;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        ((Pane) root()).getChildren().add(candle);

        chartSpan.values(0, TickSpan.class).observeNow(tick -> {
            if (candle != null) {
                candle.ticker.set(market.v.tickerBy(tick));
            }
        }).when(User.Scroll, e -> {
            if (e.getDeltaY() < 0) {
                chartSpan.ui.getSelectionModel().selectNext();
            } else {
                chartSpan.ui.getSelectionModel().selectPrevious();
            }
        });
    }
}
