/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.market.Exchange;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.util.arithmetic.Primitives;
import kiss.Disposable;
import kiss.Signal;
import kiss.Variable;
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

    private Variable<Boolean> isLoading = Variable.of(false);

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
        Viewtify.inWorker(() -> {
            isLoading.set(true);
            boolean update = chart.showRealtimeUpdate.exact();
            chart.showRealtimeUpdate.set(false);
            chart.market.set(market);
            market.readLog(log -> log.fromLast(7, LogType.Fast));
            chart.showRealtimeUpdate.set(update);
            isLoading.set(false);

            updateTab();

            TradingViewCoordinator.finishLoading(service, tab);
        });

        UserActionHelper.of(ui()).when(User.DoubleClick, () -> OrderView.ActiveMarket.set(market));
    }

    private void updateTab() {
        Disposable diposer;

        if (service == BitFlyer.FX_BTC_JPY) {
            diposer = SFD.now() //
                    .switchOff(isLoading())
                    .throttle(StaticConfig.drawingThrottle(), MILLISECONDS)
                    .diff()
                    .on(Viewtify.UIThread)
                    .to(e -> tab.text(service.id + "\n" + e.ⅰ.price + " (" + e.ⅲ.format(Primitives.DecimalScale2) + "%) "));
        } else {
            diposer = service.executionsRealtimely()
                    .startWith(service.executionLatest())
                    .switchOff(isLoading())
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
     * Get an event stream indicating whether or not this {@link TradingView} is currently reading
     * data.
     * 
     * @return
     */
    public Signal<Boolean> isLoading() {
        return isLoading.observing();
    }

    /** Load in parallel for each {@link Exchange}. */
    private static final ConcurrentHashMap<Exchange, ExchangeLoadingQueue> loaders = new ConcurrentHashMap();

    /**
     * Register the specified market in the loading queue.
     * 
     * @param service
     */
    public static Disposable requestLoading(MarketService service, Runnable starter, Runnable finisher) {
        ExchangeLoadingQueue queue = loaders.computeIfAbsent(service.exchange, ExchangeLoadingQueue::new);
        queue.items.add(new Item(starter, finisher));
        queue.startLoading();

        return queue::finishLoading;
    }

    /**
     * Load in parallel for each exchange.
     */
    private static class ExchangeLoadingQueue {

        /** The associated exchange. */
        private final Exchange exchange;

        /** The waiting list. */
        private final Deque<Item> items = new ConcurrentLinkedDeque();

        /**
         * @param exchange
         */
        private ExchangeLoadingQueue(Exchange exchange) {
            this.exchange = exchange;
        }

        /**
         * {@link TradeMate} will automatically initialize in the background if any tab has not been
         * activated yet.
         */
        private final synchronized void startLoading() {
            Item item = items.peekFirst();
            if (item != null && !item.loading) {
                item.loading = true;
                for (Runnable op : item.starters) {
                    op.run();
                }
            }
        }

        /**
         * 
         */
        private synchronized void finishLoading() {
            Item item = items.pollFirst();
            if (item != null && item.loading) {
                item.loading = false;
                for (Runnable op : item.finishers) {
                    op.run();
                }
                startLoading();
            }
        }
    }

    /**
     * 
     */
    private static class Item {

        private boolean loading;

        /** The starting operations. */
        private final List<Runnable> starters = new ArrayList();

        /** The finising operations. */
        private final List<Runnable> finishers = new ArrayList();

        /**
         * @param starter
         * @param finisher
         */
        private Item(Runnable starter, Runnable finisher) {
            starters.add(starter);
            finishers.add(finisher);
        }
    }
}