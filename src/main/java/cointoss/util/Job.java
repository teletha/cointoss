/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import kiss.WiseConsumer;

public class Job {

    /** The scheduler manager. */
    private static final Map<Object, ScheduledExecutorService> schedulers = new ConcurrentHashMap();

    public static Job by(Object key) {
        return schedulers.computeIfAbsent(key, k -> Executors.newSingleThreadScheduledExecutor(run -> {
            Thread thread = new Thread(run);
            thread.setName(k.toString());
            thread.setDaemon(true);
            return thread;
        }));
    }

    public static void run(Object key, WiseConsumer<JobProcess> job) {
        ScheduledExecutorService scheduler = schedulers.computeIfAbsent(key, k -> Executors.newSingleThreadScheduledExecutor(run -> {
            Thread thread = new Thread(run);
            thread.setName(k.toString());
            thread.setDaemon(true);
            return thread;
        }));

        scheduler.submit(() -> {
            JobProcess process = new JobProcess();

            job.accept(process);
        });
    }
}
