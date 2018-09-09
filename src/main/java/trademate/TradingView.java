/*
 * Copyright (C) 2018 CoinToss Development Team
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
import cointoss.order.Order;
import kiss.I;
import trademate.chart.ChartView;
import trademate.console.Console;
import trademate.order.OrderBookView;
import trademate.order.OrderBuilder;
import trademate.order.OrderCatalog;
import trademate.order.OrderSet;
import trademate.order.PositionCatalog;
import trademate.setting.Notificator;
import viewtify.Viewtify;
import viewtify.dsl.UIDefinition;
import viewtify.ui.View;

/**
 * @version 2018/09/09 12:53:14
 */
public class TradingView extends View {

    public final MarketService service;

    private final Notificator notificator = I.make(Notificator.class);

    public ExecutionView executionView;

    public Console console;

    public OrderBookView books;

    public OrderBuilder builder;

    public OrderCatalog orders;

    public PositionCatalog positions;

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
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                vbox(() -> {
                    hbox(() -> {
                        $(chart);
                        $(builder);
                        $(books);
                        $(executionView);
                    });
                    hbox(() -> {
                        $(orders);
                        $(positions);
                        $(console);
                    });
                });
            }
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
        return TradingView.class.getSimpleName() + View.IDSeparator + service.fullName;
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

    /**
     * Reqest order to the market.
     * 
     * @param order
     */
    public final void order(Order order) {
        OrderSet set = new OrderSet();
        set.sub.add(order);

        order(set);
    }

    /**
     * Reqest order to the market.
     * 
     * @param set
     */
    public final void order(OrderSet set) {
        // ========================================
        // Create View Model
        // ========================================
        orders.createOrderItem(set);

        // ========================================
        // Request to Server
        // ========================================
        for (Order order : set.sub) {
            Viewtify.inWorker(() -> {
                market().request(order).to(o -> {
                    // ok
                }, e -> {
                    notificator.orderFailed.notify("Reject : " + e.getMessage() + "\r\n" + order);
                });
            });
        }
    }
}
