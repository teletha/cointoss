/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import cointoss.Timelinable;
import kiss.Observer;
import kiss.Signal;

public class SignalSynchronizer {

    private final ConcurrentLinkedDeque<Timelinable> timeline = new ConcurrentLinkedDeque();

    private Observer<Timelinable> base;

    public <T extends Timelinable> Function<Signal<T>, Signal<T>> sync() {
        return signal -> {
            return new Signal<T>((observer, disposer) -> {
                signal.to(v -> {
                    if (base == null) {
                        base = (Observer<Timelinable>) observer;
                        timeline.offerLast(v);
                    } else if (base == observer) {
                        timeline.offerLast(v);
                    } else {
                        long mills = v.mills();
                        while (!timeline.isEmpty() && timeline.peekFirst().mills() <= mills) {
                            base.accept(timeline.pollFirst());
                        }
                        observer.accept(v);

                        if (timeline.isEmpty()) {
                            base = null;
                        }
                    }
                }, observer::error, () -> {
                    if (base != observer) {
                        for (Timelinable value : timeline) {
                            base.accept(value);
                        }
                    }

                    observer.complete();
                });

                return disposer;
            });
        };
    }
}