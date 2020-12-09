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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import psychopath.Locator;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeMate extends View {

    static {
        Viewtify.Terminator.add(EfficientWebSocket::shutdownNow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.register("Setting").contents(SettingView.class).closable(false);
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);
        DockSystem.register("Global Volume").contents(GlobalVolumeView.class).closable(false);
        DockSystem.register("Order").contents(OrderView.class).closable(false);

        // ========================================================
        // Create Tab for each Markets
        // ========================================================
        MarketServiceProvider.availableMarketServices().to(service -> {
            UITab tab = DockSystem.register(service.id())
                    .closable(false)
                    .text(service.marketReadableName)
                    .contents(ui -> new TradingView(ui, service));

            requestLoading(service, tab);
        });

        // ========================================================
        // Clock in Title bar
        // ========================================================
        Chrono.seconds().map(Chrono.DateDayTime::format).combineLatest(Wisdom.random()).on(Viewtify.UIThread).to(v -> {
            stage().v.setTitle(v.ⅰ + "  " + v.ⅱ);
        });
    }

    /** Load in parallel for each {@link Exchange}. */
    private final ConcurrentHashMap<Exchange, LoadingQueue> loadings = new ConcurrentHashMap();

    /**
     * Register the specified market in the loading queue.
     * 
     * @param service
     * @param tab
     */
    private final void requestLoading(MarketService service, UITab tab) {
        LoadingQueue queue = loadings.computeIfAbsent(service.exchange, key -> new LoadingQueue());
        queue.tabs.add(tab);
        queue.tryLoading();
    }

    /**
     * Unregister the specified market from the loading queue.
     * 
     * @param service
     */
    public final void finishLoading(MarketService service, UITab tab) {
        LoadingQueue queue = loadings.get(service.exchange);
        queue.tabs.remove(tab);

        if (queue.loading == tab) {
            queue.loading = null;
            queue.tryLoading();
        }
    }

    /**
     * Load in parallel for each exchange.
     */
    private static class LoadingQueue {

        private final LinkedList<UITab> tabs = new LinkedList();

        private UITab loading;

        /**
         * {@link TradeMate} will automatically initialize in the background if any tab has not been
         * activated yet.
         */
        private final synchronized void tryLoading() {
            if (loading == null && !tabs.isEmpty()) {
                loading = tabs.remove(0);
                Viewtify.inUI(loading::load);
            }
        }
    }

    /**
     * Managing words of widsom.
     */
    private static class Wisdom {

        private static final List<String> words = Locator.file("wisdom.txt").lines().toList();

        private static final Random random = new Random();

        private static Signal<String> random() {
            return Chrono.minutes().map(v -> words.get(random.nextInt(words.size())));
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application().logging(LogManager.getLogger()::error).use(Theme.Dark).icon("icon/app.png").activate(TradeMate.class);
    }
}