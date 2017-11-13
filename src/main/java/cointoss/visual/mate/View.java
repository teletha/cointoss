/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;

import kiss.Disposable;
import kiss.Extensible;

/**
 * @version 2017/11/13 20:39:23
 */
public abstract class View implements Extensible {

    /** The terminate helper. */
    static final List<Disposable> terminators = new ArrayList();

    /** The thread pool. */
    private static final ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    });

    /** Executor for UI Thread. */
    protected final Consumer<Runnable> UIThread = Platform::runLater;

    /** Executor for Worker Thread. */
    protected final Consumer<Runnable> WorkerThread = pool::submit;

    /**
     * Initialize this view.
     */
    protected abstract void initialize();

    /**
     * Execute task in pooled-background-worker thread.
     * 
     * @param process
     */
    protected void inWorker(Runnable process) {
        pool.submit(process);
    }

    /**
     * Execute task in pooled-background-worker thread.
     * 
     * @param process
     */
    protected void inWorker(Supplier<Disposable> process) {
        pool.submit(() -> {
            terminators.add(process.get());
        });
    }
}
