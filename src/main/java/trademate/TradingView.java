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

import java.util.function.Predicate;

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

    /** The associated market service. */
    public final MarketService service;

    /** The associated market. */
    public final Market market;

    public ExecutionView executionView;

    public Console console;

    public OrderBookView books;

    public OrderBuilder builder;

    public OrderCatalog orders;

    public TradeInfomationView positions;

    public ChartView chart;

    /**
     * Each View will process a large amount of logs at initialization, but using this flag can
     * greatly reduce the UI processing.
     */
    private boolean whileInit;

    /**
     * Each View will process a large amount of logs at initialization, but using this flag can
     * greatly reduce the UI processing.
     * 
     * @return
     */
    public final Predicate<Object> initializing = e -> whileInit;

    /**
     * @param service
     */
    public TradingView(MarketService service) {
        this.service = service;
        this.market = new Market(service);

        Viewtify.Terminator.add(market);
    }

    /**
     * UI definition.
     */
    class view extends UI {
        {
            $(vbox, () -> {
                $(hbox, style.fill, () -> {
                    $(chart);
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
    }

    /**
     * Style definition.
     */
    interface style extends StyleDSL {
        Style fill = () -> {
            display.height.fill().width.fill();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Viewtify.inWorker(() -> {
            chart.reduceRealtimeUpdate();
            chart.market.set(market);

            whileInit = true;
            market.readLog(log -> log.fromYestaday());
            whileInit = false;

            chart.restoreRealtimeUpdate();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + service.marketIdentity();
    }
}
