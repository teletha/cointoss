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

import org.junit.jupiter.api.Test;

class FirstCacheTimeTest extends FeatherStoreTestBase {

    @Test
    void noData() {
        FeatherStore<Value> store = createStore(1, 10, null, null);
        assert store.firstCacheTime() == -1;
    }

    @Test
    void memoryOnly() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), null);
        assert store.firstCacheTime() == 1;
    }

    @Test
    void memoryOnlyLastEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(5, 4, 3, 2, 1), null);
        assert store.firstCacheTime() == 3;
    }

    @Test
    void memoryOnlyFirstEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(1, 2, 3, 4, 5, 6), null);
        assert store.firstCacheTime() == 4;
    }

    @Test
    void memoryOnlyMiddleEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(1, 2, 3, 5, 4, 1, 2), null);
        assert store.firstCacheTime() == 3;
    }

    @Test
    void memoryOnlyUpdate() {
        FeatherStore<Value> store = createStore(1, 10, value(10, 20, 30, 40), null);
        assert store.firstCacheTime() == 10;

        store.store(value(5));
        assert store.firstCacheTime() == 5;

        store.store(value(1));
        assert store.firstCacheTime() == 1;

        store.store(value(15));
        assert store.firstCacheTime() == 1;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        assert store.firstCacheTime() == -1;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), value(1, 2, 3, 4));
        assert store.firstCacheTime() == 1;
    }

    @Test
    void memoryHasEarliestData() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4, 5), value(3, 4));
        assert store.existOnHeap(value(1));
        assert store.firstCacheTime() == 1;
    }

    @Test
    void diskHasEarliestData() {
        FeatherStore<Value> store = createStore(1, 10, value(3, 4), value(1, 2, 3));
        assert store.existOnHeap(value(1)) == false;
        assert store.firstCacheTime() == 3;
    }
}