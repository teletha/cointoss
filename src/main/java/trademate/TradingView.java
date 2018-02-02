/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import java.time.temporal.ChronoUnit;

import javafx.scene.layout.AnchorPane;

import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.TickSpan;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.I;
import trademate.chart.CandleChart;
import trademate.console.Console;
import trademate.order.OrderBookView;
import trademate.order.OrderBuilder;
import trademate.order.OrderCatalog;
import trademate.order.PositionCatalog;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIComboBox;
import viewtify.ui.UILabel;
import viewtify.ui.UITab;

/**
 * @version 2017/11/29 10:50:06
 */
public class TradingView extends View {

    public final BitFlyer provider;

    private final UITab tab;

    private final Notificator notificator = I.make(Notificator.class);

    public @UI ExecutionView executionView;

    public @UI Console console;

    public @UI OrderBookView board;

    public @UI OrderBuilder builder;

    public @UI OrderCatalog orders;

    public @UI PositionCatalog positions;

    public @UI AnchorPane chart;

    /** Chart UI */
    public @UI UIComboBox<TickSpan> chartSpan;

    /** Chart UI */
    public @UI UILabel selectDate;

    /** Chart UI */
    public @UI UILabel selectHigh;

    /** Chart UI */
    public @UI UILabel selectLow;

    /** Market cache. */
    private Market market;

    /**
     * @param tab
     */
    public TradingView(BitFlyer provider, UITab tab) {
        this.provider = provider;
        this.tab = tab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        CandleChart candleChart = new CandleChart(chart, this).graph(plot -> {
        }).axisX(axis -> {
            axis.scroll.setVisibleAmount(0.1);
            axis.scroll.setValue(1);

            long minute = 60;
            axis.tickLabelFormatter.set(time -> Chrono.systemBySeconds(time).format(Chrono.TimeWithoutSec));
            axis.units.set(new double[] {minute, 5 * minute, 10 * minute, 30 * minute, 60 * minute, 2 * 60 * minute, 4 * 60 * minute,
                    6 * 60 * minute, 12 * 60 * minute, 24 * 60 * minute});
        }).axisY(axis -> {
            axis.scroll.setVisible(false);
            axis.tickLabelFormatter.set(v -> Num.of(v).scale(0).toString());
        }).use(TickSpan.Second5);

        chartSpan.values(0, TickSpan.class).observe(candleChart::use).when(User.Scroll, e -> {
            if (e.getDeltaY() < 0) {
                chartSpan.ui.getSelectionModel().selectNext();
            } else {
                chartSpan.ui.getSelectionModel().selectPrevious();
            }
        });

        market().yourExecution.to(o -> {
            notificator.execution.notify("Executed " + o);
        });

        market().health.observe().on(Viewtify.UIThread).to(v -> {
            tab.text(market().name() + "  " + v.mark);
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + provider.fullName();
    }

    /**
     * Retrieve the associated market.
     * 
     * @return
     */
    public final synchronized Market market() {
        if (market == null) {
            Viewtify.Terminator.add(market = new Market(provider, provider.log().fromLast(14, ChronoUnit.HOURS).share()));
        }
        return market;
    }
}
