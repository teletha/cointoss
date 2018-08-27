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
import trademate.preference.Notificator;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UITab;

/**
 * @version 2018/02/07 17:12:03
 */
public class TradingView extends View {

    public final MarketService service;

    private final UITab tab;

    private final Notificator notificator = I.make(Notificator.class);

    public @UI ExecutionView executionView;

    public @UI Console console;

    public @UI OrderBookView board;

    public @UI OrderBuilder builder;

    public @UI OrderCatalog orders;

    public @UI PositionCatalog positions;

    public @UI ChartView chart;

    /** Market cache. */
    private Market market;

    /**
     * @param tab
     */
    public TradingView(MarketService service, UITab tab) {
        this.service = service;
        this.tab = tab;
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
