/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.util.Objects;
import java.util.function.LongSupplier;

import kiss.WiseSupplier;

public class Holder<V> {

    /** The data supplier. */
    private final WiseSupplier<V> supplier;

    /** The timing function. */
    private final LongSupplier timer;

    /** The time margin. */
    private final long margin;

    /** The current cache. */
    private V value;

    /** Time to live. */
    private long ttl;

    /**
     * Default data holder with 15 sec margin.
     * 
     * @param supplier
     */
    public Holder(WiseSupplier<V> supplier) {
        this(supplier, System::currentTimeMillis, 15000);
    }

    /**
     * Create data holder.
     * 
     * @param supplier
     * @param timer
     * @param margin
     */
    public Holder(WiseSupplier<V> supplier, LongSupplier timer, long margin) {
        this.supplier = Objects.requireNonNull(supplier);
        this.timer = Objects.requireNonNull(timer);
        this.margin = margin;
    }

    /**
     * Compute the current value.
     * 
     * @return
     */
    public V compute() {
        long now = timer.getAsLong();

        if (now <= ttl) {
            return value;
        }

        ttl = now + margin;
        return value = supplier.get();
    }

    /**
     * Clear cache.
     */
    public void clear() {
        ttl = 0;
    }
}