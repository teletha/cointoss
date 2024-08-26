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

class FirstIdealCacheTimeTest extends FeatherStoreTestBase {

    @Test
    void noData() {
        FeatherStore<Value> store = createStore(1, 10, null, null);
        assert store.computeLogicalFirstCacheTime() == -1;
    }

    @Test
    void memoryOnly() {
        FeatherStore<Value> store = createStore(1, 10, value(10, 11, 12, 13), null);
        assert store.computeLogicalFirstCacheTime() == 4;
    }

    @Test
    void memoryOnlyLastEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(15, 14, 13, 12, 11), null);
        assert store.computeLogicalFirstCacheTime() == 11;
    }

    @Test
    void memoryOnlyFirstEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(11, 12, 13, 14, 15, 16), null);
        assert store.computeLogicalFirstCacheTime() == 14;
    }

    @Test
    void memoryOnlyMiddleEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(11, 12, 13, 15, 14, 11, 12), null);
        assert store.computeLogicalFirstCacheTime() == 12;
    }

    @Test
    void memoryOnlyUpdate() {
        FeatherStore<Value> store = createStore(1, 10, value(10, 20, 30, 40), null);
        assert store.computeLogicalFirstCacheTime() == 31;

        store.store(value(41));
        assert store.computeLogicalFirstCacheTime() == 32;

        store.store(value(50));
        assert store.computeLogicalFirstCacheTime() == 41;

        store.store(value(10));
        assert store.computeLogicalFirstCacheTime() == 41;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        assert store.computeLogicalFirstCacheTime() == -1;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(11, 12, 13, 14), value(1, 2, 3, 4));
        assert store.computeLogicalFirstCacheTime() == 5;
    }
}