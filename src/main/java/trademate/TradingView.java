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

import java.util.function.Consumer;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.util.Primitives;
import kiss.Disposable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.order.OrderBookView;
import trademate.order.OrderBuilder;
import viewtify.Viewtify;
import viewtify.ui.UILabel;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class TradingView extends View {

    /** The market tab. */
    private final UITab tab;

    /** The associated market service. */
    public final MarketService service;

    /** The associated market. */
    public final Market market;

    /** The market title. */
    private UILabel title;

    /** The market latest price. */
    private UILabel price;

    public ExecutionView executionView;

    public OrderBookView books;

    public OrderBuilder builder;

    public ChartView chart;

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
            $(hbox, () -> {
                $(vbox, style.chartArea, () -> {
                    $(chart);
                });

                $(vbox, () -> {
                    $(hbox, () -> {
                        $(builder);
                        $(books);
                        $(executionView);
                    });
                });
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Viewtify.inWorker(() -> {
            chart.reduceRealtimeUpdate();
            chart.market.set(market);

            market.readLog(log -> log.fromLast(6, LogType.Fast));

            chart.restoreRealtimeUpdate();

            findAncestorView(TradeMate.class).to(TradeMate::requestLazyInitialization);
        });

        title.text(service.marketReadableName()).style(style.tabTitle);
        price.style(style.tabPrice);

        additionalInfo();
    }

    private void additionalInfo() {
        Disposable diposer;
        Consumer<Throwable> error = e -> {
        };

        if (service == BitFlyer.FX_BTC_JPY) {
            diposer = SFD.now() //
                    .take(chart.showRealtimeUpdate.observing())
                    .on(Viewtify.UIThread)
                    .to(e -> {
                        tab.textV(title, price.text(e.ⅰ.price + " (" + e.ⅲ.format(Primitives.DecimalScale2) + "%) " + e.ⅰ.delay));
                    }, error);
        } else {
            diposer = service.executionsRealtimely()
                    .take(chart.showRealtimeUpdate.observe())
                    .startWith(service.executionLatest())
                    .retryWhen(service.retryPolicy(100, "Title"))
                    .on(Viewtify.UIThread)
                    .to(e -> {
                        tab.textV(title, price.text(e.price));
                    }, error);
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
