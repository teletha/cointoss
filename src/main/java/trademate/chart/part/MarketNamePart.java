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

import cointoss.Market;
import javafx.scene.text.FontWeight;
import trademate.chart.ChartCanvas;
import viewtify.Theme;
import viewtify.preference.Preferences;

public class MarketNamePart extends ChartPart {

    /**
     * @param parent
     */
    public MarketNamePart(ChartCanvas parent) {
        super(parent);

        Theme theme = Preferences.theme();
        canvas.font(20, FontWeight.BOLD).fillColor(theme.textMid());

        layout.layoutBy(chartAxisModification()).layoutBy(userInterfaceModification());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChangeMarket(Market market) {
        canvas.size(180, 30).fillText(market.service.id, ChartCanvas.chartInfoLeftPadding, canvas.fontSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
    }
}
