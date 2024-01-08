/*
 * Copyright (C) 2024 The COINTOSS Development Team
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

    private Num cumulative = previous.size;

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> apply(Signal<Execution> signal) {
        return signal.map(now -> {
            Execution prev = previous;
            previous = now;

            if (prev.mills + 1000 >= now.mills && prev.direction == now.direction && prev.price.equals(now.price)) {
                cumulative = cumulative.plus(now.size);
                return null;
            } else {
                Num previousCumulative = cumulative;
                cumulative = now.size;

                if (prev.size == previousCumulative) {
                    return prev;
                } else {
                    return prev.withSize(previousCumulative);
                }
            }
        }).skip(e -> e == null || e == Market.BASE).concat(new Signal<>((observer, disposer) -> {
            if (previous.size == cumulative) {
                observer.accept(previous);
            } else {
                observer.accept(previous.withSize(cumulative));
            }
            observer.complete();
            return disposer;
        }));
    }
}