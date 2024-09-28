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
    private static final Map<Object, Job> schedulers = new ConcurrentHashMap();

    public static Job by(Object key) {
        return schedulers.computeIfAbsent(key, Job::new);
    }

    public static final Job TickerGenerator = by("TickerGenerator");

    private final ScheduledExecutorService service;

    private Job(Object key) {
        service = Executors.newSingleThreadScheduledExecutor(run -> {
            Thread thread = new Thread(run);
            thread.setName(key.toString());
            thread.setDaemon(true);
            return thread;
        });
    }

    public void run(Object key, WiseConsumer<JobProcess> job) {
        service.submit(() -> {
            JobProcess process = new JobProcess();

            job.accept(process);
        });
    }
}
