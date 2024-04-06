/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.util.Iterator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import cointoss.execution.Execution;
import hypatia.Num;
import hypatia.Orientational;
import kiss.Variable;
import kiss.WiseRunnable;

/**
 * Fast price-matching engine.
 */
public class PriceEngine {

    private final ConcurrentSkipListMap<Num, WiseRunnable> seller = new ConcurrentSkipListMap();

    private final ConcurrentSkipListMap<Num, WiseRunnable> buyer = new ConcurrentSkipListMap();

    public PriceEngine(Market market) {
        market.timeline.to(this::match);
    }

    /**
     * Matching by price.
     * 
     * @param price
     */
    private void match(Execution e) {
        ConcurrentNavigableMap<Num, WiseRunnable> matched = buyer.tailMap(e.price, true);
        if (!matched.isEmpty()) {
            Iterator<WiseRunnable> iterator = matched.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().run();
                iterator.remove();
            }
        }

        matched = seller.headMap(e.price, true);
        if (!matched.isEmpty()) {
            Iterator<WiseRunnable> iterator = matched.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().run();
                iterator.remove();
            }
        }
    }

    public void register(Variable<Num> price, Orientational direction, WiseRunnable action) {
        if (price.isFixed()) {
            if (direction.isPositive()) {
                buyer.put(price.v, action);
            } else {
                seller.put(price.v, action);
            }
        } else {
            if (direction.isPositive()) {
                buyer.put(price.v, action);
                price.observing().diff().buffer(2).to(list -> {
                    buyer.remove(list.get(0));
                    buyer.put(list.get(1), action);
                });
            } else {
                seller.put(price.v, action);
                price.observing().diff().buffer(2).to(list -> {
                    seller.remove(list.get(0));
                    seller.put(list.get(1), action);
                });
            }
        }

    }
}
