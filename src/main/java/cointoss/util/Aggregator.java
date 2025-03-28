/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.TreeMultimap;

import kiss.Signaling;

public class Aggregator<C> {

    /** The aggregator. */
    private final Signaling<C> aggregator = new Signaling();

    /**
     * Build aggregator with time-based sliding windoww.
     * 
     * @param <K>
     * @param <V>
     * @param keySelector
     * @param valueSelector
     * @param builder
     */
    public <K extends Comparable, V extends Comparable> Aggregator(Function<C, K> keySelector, Function<C, V> valueSelector, BiConsumer<K, Collection<V>> builder) {
        aggregator.expose.debounceAll(10, TimeUnit.SECONDS).to(items -> {
            TreeMultimap<K, V> map = TreeMultimap.create();

            for (C item : items) {
                map.put(keySelector.apply(item), valueSelector.apply(item));
            }

            for (Entry<K, Collection<V>> entry : map.asMap().entrySet()) {
                builder.accept(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * Send your message to aggregator.
     * 
     * @param value
     */
    public void commit(C value) {
        if (value != null) {
            aggregator.accept(value);
        }
    }
}