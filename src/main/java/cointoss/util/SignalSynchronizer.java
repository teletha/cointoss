/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import cointoss.Timelinable;
import kiss.Signal;

public class SignalSynchronizer {

    private List<Deque<Timelinable>> queues = new ArrayList();

    private long latest;

    public <T extends Timelinable> Function<Signal<T>, Signal<T>> sync() {
        return signal -> {
            return new Signal<>((observer, disposer) -> {
                Deque<Timelinable> queue = new ArrayDeque();
                queues.add(queue);

                signal.to(v -> {
                    long mills = v.mills();

                    if (mills < latest) {
                        observer.accept(v);
                    }
                }, observer::error, observer::complete);

                return disposer;
            });
        };
    }
}
