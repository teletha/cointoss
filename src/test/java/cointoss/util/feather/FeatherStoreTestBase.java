/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.ticker.Span;
import psychopath.File;
import psychopath.Locator;

class FeatherStoreTestBase {

    protected static final int days = 60 * 60 * 24;

    @RegisterExtension
    CleanRoom room = new CleanRoom();

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
            store.enableDiskStore(databaseFile());
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
            store.enableDiskStore(databaseFile());
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
     * @param value
     * @return
     */
    protected final List<Value> value(int... values) {
        return IntStream.of(values).mapToObj(this::value).collect(Collectors.toList());
    }

    /**
     * Create new sequencial values.
     * 
     * @param value
     * @return
     */
    protected final List<Value> values(int start, int end) {
        List<Value> list = new ArrayList();
        for (int i = start; i <= end; i++) {
            list.add(new Value(i));
        }
        return list;
    }

    @SuppressWarnings("preview")
    public record Value(int value) implements TemporalData {

        @Override
        public long seconds() {
            return value;
        }
    }
}
