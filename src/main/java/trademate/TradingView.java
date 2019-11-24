/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import cointoss.Market;
import cointoss.MarketService;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.console.Console;
import trademate.info.TradeInfomationView;
import trademate.order.OrderBookView;
import trademate.order.OrderBuilder;
import trademate.order.OrderCatalog;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.View;

public class TradingView extends View {

    public final MarketService service;

    public ExecutionView executionView;

    public Console console;

    public OrderBookView books;

    public OrderBuilder builder;

    public OrderCatalog orders;

    public TradeInfomationView positions;

    public ChartView chart;

    /** Market cache. */
    private Market market;

    /**
     * @param service
     */
    public TradingView(MarketService service) {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, () -> {
                    $(hbox, () -> {
                        $(chart, style.fill);
                        $(builder);
                        $(books);
                        $(executionView);
                    });
                    $(hbox, () -> {
                        $(orders);
                        $(positions);
                        $(console);
                    });
                });
            }
        };
    }

    /**
     * Style definition.
     */
    interface style extends StyleDSL {
        Style fill = () -> {
            display.height.fill().width.fill();
            margin.right(5, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        chart.market.set(market());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + service.marketIdentity();
    }

    /**
     * Retrieve the associated market.
     * 
     * @return
     */
    public final synchronized Market market() {
        if (market == null) {
            Viewtify.Terminator.add(market = new Market(service).readLog(log -> log.fromYestaday().share()));
        }
        return market;
    }
}
