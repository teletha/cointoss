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
import kiss.Signal;
import kiss.Variable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.order.OrderView;
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

    private Variable<Boolean> isLoading = Variable.of(false);

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
        Viewtify.observing(tab.selectedProperty()).to(v -> {
            chart.showRealtimeUpdate.set(v);
            chart.showChart.set(true);
        });
        Viewtify.inWorker(() -> {
            isLoading.set(true);
            boolean update = chart.showRealtimeUpdate.exact();
            chart.showRealtimeUpdate.set(false);
            chart.market.set(market);
            market.readLog(log -> log.fromLast(9, LogType.Fast));
            chart.showRealtimeUpdate.set(update);
            isLoading.set(false);

            I.make(TradeMate.class).requestLazyInitialization();
        });

        additionalInfo();

        UserActionHelper.of(ui()).when(User.DoubleClick, () -> OrderView.ActiveMarket.set(market));
    }

    private void additionalInfo() {
        Disposable diposer;
        Consumer<Throwable> error = e -> {
        };

        tab.style("multiline");

        if (service == BitFlyer.FX_BTC_JPY) {
            diposer = SFD.now() //
                    .switchOff(isLoading())
                    .diff()
                    .on(Viewtify.UIThread)
                    .to(e -> tab.text(service.marketReadableName + "\n" + e.ⅰ.price + " (" + e.ⅲ
                            .format(Primitives.DecimalScale2) + "%) "), error);
        } else {
            diposer = service.executionsRealtimely()
                    .switchOff(isLoading())
                    .startWith(service.executionLatest())
                    .diff()
                    .on(Viewtify.UIThread)
                    .to(e -> tab.text(service.marketReadableName + "\n" + e.price), error);
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

    /**
     * Get an event stream indicating whether or not this {@link TradingView} is currently reading
     * data.
     * 
     * @return
     */
    public Signal<Boolean> isLoading() {
        return isLoading.observing();
    }
}