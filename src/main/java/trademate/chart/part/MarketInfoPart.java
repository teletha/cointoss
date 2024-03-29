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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import cointoss.CurrencySetting;
import cointoss.analyze.OnlineStats;
import cointoss.util.Chrono;
import hypatia.Num;
import hypatia.Primitives;
import kiss.I;
import kiss.Variable;
import trademate.chart.ChartCanvas;
import trademate.setting.PerformanceSetting;
import viewtify.Viewtify;
import viewtify.preference.Preferences;

public class MarketInfoPart extends ChartPart {

    /** Infomation Color */
    private static final Color WarningColor = Color.rgb(193, 95, 82);

    /** The market info's label. */
    private static final Variable<String> DelayLabel = I.translate("Delay");

    /** The market info's label. */
    private static final Variable<String> SpreadLabel = I.translate("Spread");

    /** The market info's label. */
    private static final Variable<String> VolatilityLabel = I.translate("Volatility");

    /**
     * @param parent
     */
    public MarketInfoPart(ChartCanvas parent) {
        super(parent);

        Font title = Font.font(null, FontWeight.BOLD, 20);
        Font normal = Font.font(null, FontWeight.BOLD, 11);

        parent.chart.market.observing()
                .skipNull()
                .switchMap(m -> m.tickers.latest.observing())
                .plug(PerformanceSetting.applyUIRefreshRate())
                .switchOn(parent.chart.showRealtimeUpdate.observing())
                .on(Viewtify.UIThread)
                .to(e -> {
                    int offset = 80;
                    int verticalOffset = 35;
                    Color textColor = Preferences.theme().textMid();

                    CurrencySetting base = parent.chart.market.v.service.setting.base;
                    GraphicsContext c = canvas.clear().getGraphicsContext2D();

                    c.setFont(title);
                    c.setFill(textColor);
                    c.fillText(parent.chart.market.v.service.id, ChartCanvas.chartInfoLeftPadding, 20);
                    c.setFont(normal);

                    long diff = Chrono.currentTimeMills() - e.mills;
                    c.setFill(diff < 0 || 1000 < diff ? WarningColor : textColor);
                    c.fillText(DelayLabel.v + " " + diff + "ms", ChartCanvas.chartInfoLeftPadding, verticalOffset);

                    double spread = parent.chart.market.v.orderBook.spread();
                    Num range = base.minimumSize.multiply(100);
                    c.setFill(spread < range.doubleValue() ? textColor : WarningColor);
                    c.fillText(SpreadLabel.v + " " + Primitives
                            .roundString(spread, base.scale), ChartCanvas.chartInfoLeftPadding + offset, verticalOffset);

                    OnlineStats volatilityStats = parent.chart.ticker.v.spreadStats;
                    double volatility = parent.chart.ticker.v.ticks.last().spread();
                    c.setFill(volatilityStats.calculateSigma(volatility) <= 2 ? textColor : WarningColor);
                    c.fillText(VolatilityLabel.v + " " + Primitives.roundString(volatility, base.scale) + " (" + Primitives
                            .roundString(volatilityStats.getMean(), base.scale) + "-" + Primitives.roundString(volatilityStats
                                    .sigma(2), base.scale) + ")", ChartCanvas.chartInfoLeftPadding + offset * 2, verticalOffset);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
    }
}
