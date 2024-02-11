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

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import kiss.WiseConsumer;
import trademate.TradeMate;

public class Coordinator {

    /** The waiting queue for each keys. */
    private static final ConcurrentHashMap<Object, Coordinator> coordinators = new ConcurrentHashMap();

    /**
     * Register the specified market in the loading queue.
     * 
     * @param key
     * @param task
     */
    public static void request(Object key, WiseConsumer<Runnable> task) {
        Coordinator coodinator = coordinators.computeIfAbsent(key, k -> new Coordinator());
        coodinator.tasks.add(task);
        coodinator.tryProcess();
    }

    /** The waiting list. */
    private final LinkedList<WiseConsumer<Runnable>> tasks = new LinkedList();

    /** The current processing task. */
    private WiseConsumer<Runnable> processing;

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
            processing.accept(() -> {
                processing = null;
                tryProcess();
            });
        }
    }
}