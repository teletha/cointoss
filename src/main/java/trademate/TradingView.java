/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static java.util.concurrent.TimeUnit.*;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.util.Coordinator;
import cointoss.util.arithmetic.Primitives;
import kiss.Disposable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.order.OrderView;
import trademate.setting.StaticConfig;
import viewtify.Viewtify;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;
import viewtify.ui.helper.UserActionHelper;

public class TradingView extends View {

    /** The market tab. */
    public final UITab tab;

    /** The associated market service. */
    public final MarketService service;

    /** The associated market. */
    public final Market market;

    public ChartView chart;

    /**
     * @param tab
     * @param service
     */
    public TradingView(UITab tab, MarketService service) {
        this.tab = tab;
        this.service = service;
        this.market = Market.of(service);

        Viewtify.Terminator.add(market);
    }

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(sbox, () -> {
                $(chart, style.chartArea);
            });
        }
    }

    /**
     * Style definition.
     */
    interface style extends StyleDSL {
        Style chartArea = () -> {
            display.height.fill().width.fill();
        };

        Style order = () -> {
            position.left(0, px).bottom(0, px);
            display.height(100, px).width(100, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        tab.style("multiline");

        Viewtify.observing(tab.selectedProperty()).to(chart.showRealtimeUpdate::set);

        chart.showRealtimeUpdate.set(false);
        Coordinator.request(service, next -> {
            market.readLog(x -> x.fromLast(7, LogType.Fast));

            chart.market.set(market);
            chart.showRealtimeUpdate.set(true);
            updateTab();

            next.run();
        });

        UserActionHelper.of(ui()).when(User.DoubleClick, () -> OrderView.ActiveMarket.set(market));
    }

    private void updateTab() {
        Disposable diposer;

        if (service == BitFlyer.FX_BTC_JPY) {
            diposer = SFD.now() //
                    .throttle(StaticConfig.drawingThrottle(), MILLISECONDS)
                    .diff()
                    .on(Viewtify.UIThread)
                    .to(e -> tab.text(service.id + "\n" + e.ⅰ.price + " (" + e.ⅲ.format(Primitives.DecimalScale2) + "%) "));
        } else {
            diposer = service.executionsRealtimely()
                    .startWith(service.executionLatest())
                    .throttle(StaticConfig.drawingThrottle(), MILLISECONDS)
                    .diff()
                    .on(Viewtify.UIThread)
                    .to(e -> tab.text(service.id + "\n" + e.price));
        }
        service.add(diposer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + service.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tab.dispose();
        chart.dispose();
    }
}