/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.chart.part;

import java.util.concurrent.TimeUnit;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import trademate.ChartTheme;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
import trademate.setting.PerformanceSetting;
import viewtify.preference.Preferences;

public class CandlePart extends ChartPart {

    private final ChartView chart;

    /**
     * @param parent
     */
    public CandlePart(ChartCanvas parent, ChartView chart) {
        super(parent);
        this.chart = chart;

        canvas.strokeColor(Color.WHITESMOKE.deriveColor(0, 1, 1, 0.35)).font(8).textBaseLine(VPos.CENTER);

        PerformanceSetting performance = Preferences.of(PerformanceSetting.class);

        layout.layoutBy(chartAxisModification())
                .layoutBy(userInterfaceModification())
                .layoutBy(chart.candleType.observe(), chart.ticker.observe(), chart.showCandle.observe())
                .layoutBy(chart.ticker.observe().switchMap(ticker -> ticker.open.throttle(performance.refreshRate, TimeUnit.MILLISECONDS)))
                .layoutBy(ChartTheme.$.buy.observe(), ChartTheme.$.sell.observe())
                .layoutWhile(chart.showRealtimeUpdate.observing());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
    }
}