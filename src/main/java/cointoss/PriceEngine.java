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
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.execution.Execution;
import hypatia.Num;
import hypatia.Orientational;
import kiss.Variable;
import kiss.WiseRunnable;

/**
 * Fast price-matching engine.
 */
public class PriceEngine {

    private final ConcurrentSkipListMap<Num, List<WiseRunnable>> seller = new ConcurrentSkipListMap();

    private final ConcurrentSkipListMap<Num, List<WiseRunnable>> buyer = new ConcurrentSkipListMap();

    public PriceEngine(Market market) {
        market.timeline.to(this::match);
    }

    /**
     * Matching by price.
     * 
     * @param price
     */
    private void match(Execution e) {
        ConcurrentNavigableMap<Num, List<WiseRunnable>> matched = buyer.tailMap(e.price, true);
        if (!matched.isEmpty()) {
            Iterator<List<WiseRunnable>> iterator = matched.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().forEach(Runnable::run);
                iterator.remove();
            }
        }

        matched = seller.headMap(e.price, true);
        if (!matched.isEmpty()) {
            Iterator<List<WiseRunnable>> iterator = matched.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().forEach(Runnable::run);
                iterator.remove();
            }
        }
    }

    /**
     * Register the priced action.
     * 
     * @param price
     * @param orientational
     * @param action
     */
    public void register(Num price, Orientational orientational, WiseRunnable action) {
        register(Variable.of(price).fix(), orientational, action);
    }

    /**
     * Register the priced action.
     * 
     * @param price
     * @param orientational
     * @param action
     */
    public void register(Variable<Num> price, Orientational orientational, WiseRunnable action) {
        if (orientational.isPositive()) {
            register(buyer, price.v, action);
            if (!price.isFixed()) {
                price.observing().diff().buffer(2).to(list -> {
                    unregister(buyer, list.get(0), action);
                    register(buyer, list.get(1), action);
                });
            }
        } else {
            register(seller, price.v, action);
            if (!price.isFixed()) {
                price.observing().diff().buffer(2).to(list -> {
                    unregister(seller, list.get(0), action);
                    register(seller, list.get(1), action);
                });
            }
        }
    }

    private void register(ConcurrentSkipListMap<Num, List<WiseRunnable>> waitings, Num price, WiseRunnable action) {
        waitings.computeIfAbsent(price, p -> new CopyOnWriteArrayList()).add(action);
    }

    private void unregister(ConcurrentSkipListMap<Num, List<WiseRunnable>> waitings, Num price, WiseRunnable action) {
        List<WiseRunnable> actions = waitings.remove(price);
        if (actions != null) {
            actions.remove(action);
            if (actions.isEmpty()) {
                waitings.remove(price);
            }
        }
    }
}
