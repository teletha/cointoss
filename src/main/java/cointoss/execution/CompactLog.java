/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.util.function.Function;

import cointoss.Market;
import cointoss.util.arithmetic.Num;
import kiss.Signal;

class CompactLog implements Function<Signal<Execution>, Signal<Execution>> {

    private Execution previous = Market.BASE;

    private Num accumlative = previous.size;

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> apply(Signal<Execution> signal) {
        return signal.map(now -> {
            Execution prev = previous;
            previous = now;

            if (prev.mills == now.mills && prev.direction == now.direction && prev.price.equals(now.price)) {
                accumlative = accumlative.plus(now.size);
                return null;
            } else {
                Num previousAccumulative = accumlative;
                accumlative = now.size;

                if (prev.size == previousAccumulative) {
                    return prev;
                } else {
                    return prev.withSize(previousAccumulative);
                }
            }
        }).skipNull().concat(new Signal<>((observer, disposer) -> {
            if (previous.size == accumlative) {
                observer.accept(previous);
            } else {
                observer.accept(previous.withSize(accumlative));
            }
            observer.complete();
            return disposer;
        }));
    }
}