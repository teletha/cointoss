/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.ticker.Span;
import psychopath.File;
import psychopath.Locator;
import typewriter.api.model.IdentifiableModel;

class FeatherStoreTestBase {

    protected static final int days = 60 * 60 * 24;

    @RegisterExtension
    CleanRoom room = new CleanRoom(true);

    protected final File databaseFile() {
        return Locator.file(room.locateRadom());
    }

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
            store.enablePersistence(databaseFile());
            store.store(initialDiskValues);
            store.commit();
            store.clearHeap();
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
            store.enablePersistence(RandomStringUtils.randomAlphanumeric(30));
            store.store(initialDiskValues);
            store.commit();
            store.clearHeap();
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

        public int value;

        public Value() {
        }

        public Value(int value) {
            this.value = value;
        }

        @Override
        public long seconds() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getId() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setId(long id) {
            value = (int) id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(value);
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Value other = (Value) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance())) return false;
            return value == other.value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Value [value=" + value + "]";
        }

        private FeatherStoreTestBase getEnclosingInstance() {
            return FeatherStoreTestBase.this;
        }
    }
}