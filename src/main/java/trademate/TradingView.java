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

import java.time.ZonedDateTime;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.util.DateRange;
import cointoss.util.Mediator;
import kiss.I;
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
import viewtify.ui.toast.Toast;

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

        Mediator.ExecutionCollector.schedule(service, () -> {
            market.readLog(x -> x.fromLast(3, LogType.Fast).concat(service.executions()));
            buildTicker(false);

            chart.market.set(market);
            chart.showRealtimeUpdate.set(tab.isSelected());
            chart.ticker.observing().to(ticker -> chart.chart.layoutForcely());
        });

        UserActionHelper.of(ui()).when(User.DoubleClick, () -> OrderView.ActiveMarket.set(market));
    }

    /**
     * Build ticker.
     */
    public void buildTicker(boolean forceRebuild) {
        buildTicker(market.tickers.estimateFullBuild(), forceRebuild, false);
    }

    /**
     * Build ticker.
     */
    public void buildTicker(DateRange range, boolean forceRebuild, boolean forceRebuildLog) {
        Mediator.TickerGenerator.schedule(() -> {
            Toast<ZonedDateTime> toast = Toast.<ZonedDateTime> title(en("Build ticker from historical log."))
                    .totalProgress(range.countDays() + 1)
                    .whenCompleted(chart.chart::layoutForcely)
                    .whenProgressed((m, date) -> {
                        String text = service + " [" + date.toLocalDate() + "]";
                        I.debug("Build ticker on " + text);
                        m.message(text).setProgress(range.countDaysFrom(date));
                    });

            market.tickers.enumerateBuildableDate(range, forceRebuild)
                    .plug(toast)
                    .effect(date -> market.tickers.build(date, forceRebuildLog))
                    .to(I.NoOP);
        });
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