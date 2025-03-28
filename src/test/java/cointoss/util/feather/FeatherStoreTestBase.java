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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;

import cointoss.ticker.Span;
import typewriter.api.model.IdentifiableModel;

class FeatherStoreTestBase {

    protected static final int days = 60 * 60 * 24;

    /**
     * Create empty store.
     * 
     * @return
     */
    protected final FeatherStore<Value> createStore(Span span) {
        return createStore(span, null, null);
    }

    /**
     * Create store with initial values.
     * 
     * @return
     */
    protected final FeatherStore<Value> createStore(Span span, List<Value> initialMemoryValues, List<Value> initialDiskValues) {
        FeatherStore<Value> store = FeatherStore.create(Value.class, span);
        if (initialDiskValues != null) {
            store.enablePersistence(RandomStringUtils.secure().nextAlphabetic(30));
            store.store(initialDiskValues);
            store.commit();
            store.clear();
        }

        if (initialMemoryValues != null) {
            store.store(initialMemoryValues);
        }

        return store;
    }

    /**
     * Create store with initial values.
     * 
     * @return
     */
    protected final FeatherStore<Value> createStore(List<Value> initialMemoryValues, List<Value> initialDiskValues) {
        return createStore(10, 10, initialMemoryValues, initialDiskValues);
    }

    /**
     * Create store with initial values.
     * 
     * @return
     */
    protected final FeatherStore<Value> createStore(int itemSize, int segmentSize, List<Value> initialMemoryValues, List<Value> initialDiskValues) {
        FeatherStore<Value> store = FeatherStore.create(Value.class, 1, itemSize, segmentSize);
        if (initialDiskValues != null) {
            store.enablePersistence(RandomStringUtils.secure().nextAlphabetic(30));
            store.store(initialDiskValues);
            store.commit();
            store.clear();
        }

        if (initialMemoryValues != null) {
            store.store(initialMemoryValues);
        }

        return store;
    }

    protected final Value day(int size) {
        return value(size * days);
    }

    /**
     * Create new value.
     * 
     * @param value
     * @return
     */
    protected final Value value(int value) {
        return new Value(value);
    }

    /**
     * Create new values.
     * 
     * @param values
     * @return
     */
    protected final List<Value> value(int... values) {
        return IntStream.of(values).mapToObj(this::value).collect(Collectors.toList());
    }

    /**
     * Create new sequencial values.
     */
    protected final List<Value> values(int start, int end) {
        List<Value> list = new ArrayList();
        for (int i = start; i <= end; i++) {
            list.add(new Value(i));
        }
        return list;
    }

    public class Value extends IdentifiableModel implements Timelinable {

        public int item;

        public Value() {
        }

        public Value(int value) {
            this.item = value;
        }

        @Override
        public long seconds() {
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getId() {
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setId(long id) {
            item = (int) id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value other) {
                return item == other.item;
            } else {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Value [value=" + item + "]";
        }
    }
}