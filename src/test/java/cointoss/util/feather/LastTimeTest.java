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

class LastTimeTest extends FeatherStoreTestBase {

    @Test
    void noData() {
        FeatherStore<Value> store = createStore(1, 10, null, null);
        assert store.lastTime() == -1;
    }

    @Test
    void memoryOnly() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), null);
        assert store.lastTime() == 4;
    }

    @Test
    void memoryOnlyLastEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(5, 4, 3, 2, 1), null);
        assert store.lastTime() == 3;
    }

    @Test
    void memoryOnlyfirstEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(1, 2, 3, 4, 5, 6), null);
        assert store.lastTime() == 6;
    }

    @Test
    void memoryOnlyMiddleEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(1, 2, 3, 5, 4, 1, 2), null);
        assert store.lastTime() == 4;
    }

    @Test
    void memoryOnlyUpdate() {
        FeatherStore<Value> store = createStore(1, 10, null, value(10, 20, 30, 40));
        assert store.lastTime() == 40;

        store.store(value(50));
        assert store.lastTime() == 50;

        store.store(value(100));
        assert store.lastTime() == 100;

        store.store(value(15));
        assert store.lastTime() == 100;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        assert store.lastTime() == 4;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), value(1, 2, 3, 4));
        assert store.lastTime() == 4;
    }

    @Test
    void memoryHasLatestData() {
        FeatherStore<Value> store = createStore(1, 10, value(3, 4, 5), value(1, 2, 3, 4));
        assert store.existOnHeap(value(5));
        assert store.lastTime() == 5;
    }

    @Test
    void diskHasLatestData() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3), value(3, 4, 5));
        assert store.existOnHeap(value(5)) == false;
        assert store.lastTime() == 5;
    }
}