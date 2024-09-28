/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.MarketService;
import cointoss.market.Exchange;
import kiss.I;
import kiss.WiseConsumer;
import kiss.WiseRunnable;
import kiss.Ⅱ;
import trademate.TradeMate;
import viewtify.Viewtify;

public class Coordinator {

    /** The waiting queue for each keys. */
    private static final ConcurrentHashMap<Exchange, Coordinator> coordinators = new ConcurrentHashMap();

    private static final ConcurrentHashMap<MarketService, List<WiseRunnable>> finishers = new ConcurrentHashMap();

    /**
     * Register the specified market in the loading queue.
     * 
     * @param service
     * @param task
     */
    public static void request(MarketService service, WiseConsumer<Runnable> task) {
        Viewtify.inWorker(() -> {
            Coordinator coodinator = coordinators.computeIfAbsent(service.exchange, k -> new Coordinator());
            coodinator.tasks.add(I.pair(service, task, new ArrayList()));
            coodinator.tryProcess();
        });
    }

    /** The waiting list. */
    private final LinkedList<Ⅱ<MarketService, WiseConsumer<Runnable>>> tasks = new LinkedList();

    /** The current processing task. */
    private Ⅱ<MarketService, WiseConsumer<Runnable>> processing;

    /**
     * Hide constructor.
     */
    private Coordinator() {
    }

    /**
     * {@link TradeMate} will automatically initialize in the background if any tab has not been
     * activated yet.
     */
    private synchronized void tryProcess() {
        if (processing == null && !tasks.isEmpty()) {
            processing = tasks.remove(0);
            processing.ⅱ.accept(() -> {
                List<WiseRunnable> list = finishers.get(processing.ⅰ);
                if (list != null) {
                    list.forEach(WiseRunnable::run);
                    finishers.remove(processing.ⅰ);
                }
                processing = null;
                tryProcess();
            });
        }
    }
}