/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart.part;

import javafx.scene.text.FontWeight;

import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
import viewtify.Theme;
import viewtify.preference.Preferences;

public class MarketNamePart extends ChartPart {

    /**
     * @param parent
     */
    public MarketNamePart(ChartCanvas parent, ChartView chart) {
        super(parent);

        Theme theme = Preferences.theme();
        canvas.font(20, FontWeight.BOLD).fillColor(theme.textMid());

        parent.chart.market.observing().skipNull().switchOn(parent.chart.showRealtimeUpdate.observing()).to(market -> {
            canvas.size(180, 30).fillText(market.service.id, ChartCanvas.chartInfoLeftPadding, canvas.fontSize());
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
    }
}
