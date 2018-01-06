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

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javafx.scene.layout.AnchorPane;

import cointoss.Execution;
import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import kiss.I;
import trademate.chart.CandleChart;
import trademate.console.Console;
import trademate.order.OrderBookView;
import trademate.order.OrderBuilder;
import trademate.order.OrderCatalog;
import trademate.order.PositionCatalog;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UITab;

/**
 * @version 2017/11/29 10:50:06
 */
public class TradingView extends View {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

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
        market().yourExecution.to(o -> {
            notificator.execution.notify("Executed " + o);
        });

        market().health.on(Viewtify.UIThread).to(v -> {
            tab.text(market().name() + "  " + v.mark);
        });

        CandleChart candleChart = new CandleChart(chart, this).graph(plot -> {
        }).axisX(axis -> {
            axis.scroll.setVisibleAmount(0.15);
            axis.scroll.setValue(1);

            long minute = 60000;
            axis.tickLabelFormatter
                    .set(v -> Instant.ofEpochMilli((long) v).plus(9, ChronoUnit.HOURS).atZone(Execution.UTC).format(formatter));
            axis.units.set(new double[] {minute, 5 * minute, 10 * minute, 30 * minute, 60 * minute, 2 * 60 * minute, 4 * 60 * minute,
                    6 * 60 * minute, 12 * 60 * minute, 24 * 60 * minute});
        }).axisY(axis -> {
            axis.scroll.setVisible(false);
            axis.tickLabelFormatter.set(v -> Num.of(v).toString());
        }).candleDate(market().second5);
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
            Viewtify.Terminator.add(market = new Market(provider.service(), provider.log().fromLast(120, ChronoUnit.MINUTES)));
        }
        return market;
    }
}
