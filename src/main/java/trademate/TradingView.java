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

import java.time.Period;
import java.time.ZonedDateTime;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.util.Coordinator;
import cointoss.util.Job;
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
        Viewtify.observing(tab.selectedProperty()).to(chart.showRealtimeUpdate::set);

        chart.showRealtimeUpdate.set(false);
        Coordinator.request(service, next -> {
            market.readLog(x -> x.fromLast(3, LogType.Fast).subscribeOn(Viewtify.WorkerThread).concat(service.executions()));

            Job.TickerGenerator.run(service.exchange, job -> {

                ZonedDateTime[] dates = market.tickers.estimateFullBuild();
                Period period = Period.between(dates[0].toLocalDate(), dates[1].toLocalDate());
                int size = period.getDays();

                // ToastMonitor.show(en("Build ticker data."), size, market.tickers.build(dates[0],
                // dates[1], false)
                // .map(e -> service + " build ticker [" + e + "]")
                // .effect(e -> I.info(e)));
                // market.tickers.buildFully(false).to(e -> {
                // I.info(service + " builds ticker [" + e + "]");
                // });
            });

            chart.market.set(market);
            chart.showRealtimeUpdate.set(tab.isSelected());
            chart.ticker.observing().to(ticker -> chart.chart.layoutForcely());

            next.run();
        });

        UserActionHelper.of(ui()).when(User.DoubleClick, () -> OrderView.ActiveMarket.set(market));
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