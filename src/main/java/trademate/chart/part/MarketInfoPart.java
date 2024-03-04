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

import java.util.concurrent.TimeUnit;

import cointoss.CurrencySetting;
import cointoss.Market;
import cointoss.analyze.OnlineStats;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import cointoss.util.arithmetic.Primitives;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import kiss.I;
import kiss.Variable;
import trademate.chart.ChartCanvas;
import trademate.chart.ChartView;
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
    public MarketInfoPart(ChartCanvas parent, ChartView chart) {
        super(parent);

        canvas.font(11, FontWeight.BOLD).fillColor(Preferences.theme().textMid());

        layout.layoutBy(chartAxisModification()).layoutBy(userInterfaceModification());

        Color textColor = Preferences.theme().textMid();

        chart.market.observing()
                .skipNull()
                .switchMap(m -> m.tickers.latest.observing())
                .switchOn(chart.showRealtimeUpdate.observing())
                .throttle(1000, TimeUnit.MILLISECONDS)
                .on(Viewtify.UIThread)
                .to(e -> {
                    CurrencySetting base = chart.market.v.service.setting.base;
                    GraphicsContext c = canvas.clear().getGraphicsContext2D();

                    c.setFill(textColor);
                    c.fillText(DelayLabel.v, ChartCanvas.chartInfoLeftPadding, 35);
                    c.fillText(SpreadLabel.v, ChartCanvas.chartInfoLeftPadding, 50);
                    c.fillText(VolatilityLabel.v, ChartCanvas.chartInfoLeftPadding, 65);

                    long diff = Chrono.currentTimeMills() - e.mills;
                    c.setFill(diff < 0 || 1000 < diff ? WarningColor : textColor);
                    c.fillText(diff + "ms", 50, 35);

                    double spread = chart.market.v.orderBook.spread();
                    Num range = base.minimumSize.multiply(100);
                    c.setFill(spread < range.doubleValue() ? textColor : WarningColor);
                    c.fillText(Primitives.roundString(spread, base.scale), 50, 50);

                    OnlineStats volatilityStats = chart.ticker.v.spreadStats;
                    double volatility = chart.ticker.v.ticks.last().spread();
                    c.setFill(volatilityStats.calculateSigma(volatility) <= 2 ? textColor : WarningColor);
                    c.fillText(Primitives.roundString(volatility, base.scale), 50, 65);
                    c.setFill(textColor);
                    c.fillText("(" + Primitives.roundString(volatilityStats.getMean(), base.scale) + "-" + Primitives
                            .roundString(volatilityStats.sigma(2), base.scale) + ")", 85, 65);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChangeMarket(Market market) {
        super.onChangeMarket(market);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw() {
    }
}
