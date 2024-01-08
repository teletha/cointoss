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

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.MarketService;
import cointoss.market.Exchange;
import viewtify.Viewtify;
import viewtify.ui.UITab;

public class TradingViewCoordinator {

    /** Load in parallel for each {@link Exchange}. */
    private static final ConcurrentHashMap<Exchange, LoadingQueue> loadings = new ConcurrentHashMap();

    /**
     * Register the specified market in the loading queue.
     * 
     * @param service
     * @param tab
     */
    public static void requestLoading(MarketService service, UITab tab) {
        LoadingQueue queue = loadings.computeIfAbsent(service.exchange, key -> new LoadingQueue());
        queue.tabs.add(tab);
        queue.tryLoading();
    }

    /**
     * Unregister the specified market from the loading queue.
     * 
     * @param service
     */
    public static void finishLoading(MarketService service, UITab tab) {
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
}