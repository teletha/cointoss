/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.market.Exchange;
import kiss.Disposable;
import kiss.Managed;
import kiss.Singleton;

@Managed(Singleton.class)
public class TradingViewCoordinator {

    /** Load in parallel for each {@link Exchange}. */
    private final ConcurrentHashMap<Exchange, ExchangeLoadingQueue> loaders = new ConcurrentHashMap();

    /**
     * Load the specified market.
     * 
     * @param service
     */
    public void load(MarketService service) {
        requestLoading(service, () -> {
            Market.of(service).readLog(log -> log.fromToday(LogType.Fast));
        }, () -> {

        });
    }

    /**
     * Register the specified market in the loading queue.
     * 
     * @param service
     */
    public Disposable requestLoading(MarketService service, Runnable starter, Runnable finisher) {
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
