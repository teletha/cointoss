/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.maid;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kiss.I;

public class Main {

    public static void main(String[] args) {
        ScheduledExecutorService exe = Executors.newScheduledThreadPool(3);
        ScheduledFuture<Object> future = exe.schedule(() -> {
            throw new Error();
        }, 10, TimeUnit.MILLISECONDS);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(future.isDone() + "   " + future.state() + "  " + future.exceptionNow());
            throw I.quiet(e);
        }
    }
}
