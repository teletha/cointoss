/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.function.Consumer;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.util.Primitives;
import kiss.Disposable;
import kiss.I;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.order.OrderBuilder;
import viewtify.Viewtify;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIHBox;
import viewtify.ui.UILabel;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class TradingView extends View {

    /** The market tab. */
    public final UITab tab;

    /** The associated market service. */
    public final MarketService service;

    /** The associated market. */
    public final Market market;

    /** The market title. */
    private UILabel title;

    /** The market latest price. */
    private UILabel price;

    private UIHBox box;

    public ChartView chart;

    public ExecutionView executions;

    public OrderBuilder builder;

    private UICheckBox showExecution;

    private UICheckBox showOrderBuilder;

    public boolean whileLoading = false;

    /**
     * @param tab
     * @param service
     */
    public TradingView(UITab tab, MarketService service) {
        this.tab = tab;
        this.service = service;
        this.market = new Market(service);

        Viewtify.Terminator.add(market);
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {

            $(box, () -> {
                $(vbox, style.chartArea, () -> {
                    $(chart);
                });

                $(executions);
                $(builder);
            });
        }
    }

    /**
     * Style definition.
     */
    interface style extends StyleDSL {
        Style tabTitle = () -> {
            font.size(11, px);
        };

        Style tabPrice = () -> {
            font.size(11, px);
        };

        Style chartArea = () -> {
            display.height.fill().width.fill();
        };

        Style fill = () -> {
            display.height.fill();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        configContextMenuOnTab();

        Viewtify.observing(tab.selectedProperty()).to(chart.showRealtimeUpdate::set);
        Viewtify.inWorker(() -> {
            whileLoading = true;
            boolean update = chart.showRealtimeUpdate.exact();
            chart.showRealtimeUpdate.set(false);
            chart.market.set(market);
            market.readLog(log -> log.fromLast(9, LogType.Fast));
            chart.showRealtimeUpdate.set(update);
            whileLoading = false;

            I.make(TradeMate.class).requestLazyInitialization();
        });

        title.text(service.marketReadableName()).style(style.tabTitle);
        price.style(style.tabPrice);

        additionalInfo();
    }

    /**
     * Configuration for tab's context menu.
     */
    private void configContextMenuOnTab() {
        tab.context(c -> {
            c.menu(showExecution, false);
            c.menu(showOrderBuilder, false);
        });

        showExecution.text(en("Trade History")).initialize(true).observing(executions::visible);
        showOrderBuilder.text(en("Order")).initialize(true).observing(builder::visible);
    }

    private void additionalInfo() {
        Disposable diposer;
        Consumer<Throwable> error = e -> {
        };

        if (service == BitFlyer.FX_BTC_JPY) {
            diposer = SFD.now() //
                    .skip(v -> whileLoading)
                    .diff()
                    .on(Viewtify.UIThread)
                    .effectOnce(e -> tab.textV(title, price))
                    .to(e -> price.text(e.ⅰ.price + " (" + e.ⅲ.format(Primitives.DecimalScale2) + "%) " + e.ⅰ.delay), error);
        } else {
            diposer = service.executionsRealtimely()
                    .skip(v -> whileLoading)
                    .startWith(service.executionLatest())
                    .diff()
                    .retryWhen(service.retryPolicy(100, "Title"))
                    .on(Viewtify.UIThread)
                    .effectOnce(e -> tab.textV(title, price))
                    .to(e -> price.text(e.price), error);
        }
        service.add(diposer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + service.marketIdentity();
    }
}