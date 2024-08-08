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

class FirstIdealCacheTest extends FeatherStoreTestBase {

    @Test
    void firstIdealCache() {
        FeatherStore<Value> store = createStore(1, 5, value(1, 2, 3, 4, 5), null);
        assert store.firstIdealCache().value == 1;

        store.store(value(6));
        assert store.firstIdealCache().value == -1; // 2 was evicted automatically
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), value(1, 2, 3, 4));
        assert store.firstIdealCache().value == 1;
    }

    @Test
    void memoryHasOlderData() {
        FeatherStore<Value> store = createStore(1, 10, value(3, 4, 5), value(4, 5, 6));
        assert store.existOnHeap(value(3));
        assert store.firstIdealCache().value == 3;
    }

    @Test
    void diskHasOlderData() {
        FeatherStore<Value> store = createStore(1, 10, value(4, 5, 6), value(3, 4, 5));
        assert store.existOnHeap(value(3)) == false;
        assert store.firstIdealCache().value == 3;
    }
}