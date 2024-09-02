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

class IdealSegmentTimeTest extends FeatherStoreTestBase {

    @Test
    void noData() {
        long now = System.currentTimeMillis() / 1000;
        FeatherStore<Value> store = createStore(1, 10, null, null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == now;
        assert time[0] == now - 9;
    }

    @Test
    void memoryOnly() {
        FeatherStore<Value> store = createStore(1, 10, value(10, 11, 12, 13), null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 13;
        assert time[0] == 4;
    }

    @Test
    void memoryOnlyLastEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(15, 14, 13, 12, 11), null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 13;
        assert time[0] == 11;
    }

    @Test
    void memoryOnlyFirstEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(11, 12, 13, 14, 15, 16), null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 16;
        assert time[0] == 14;
    }

    @Test
    void memoryOnlyMiddleEviction() {
        FeatherStore<Value> store = createStore(1, 3, value(11, 12, 13, 15, 14, 11, 12), null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 14;
        assert time[0] == 12;
    }

    @Test
    void memoryOnlyUpdate() {
        FeatherStore<Value> store = createStore(1, 10, value(10, 20, 30, 40), null);
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 40;
        assert time[0] == 31;

        store.store(value(41));
        assert time[1] == 40;
        assert time[0] == 31;

        store.store(value(50));
        assert time[1] == 40;
        assert time[0] == 31;

        store.store(value(10));
        assert time[1] == 40;
        assert time[0] == 31;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 4;
        assert time[0] == -5;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(11, 12, 13, 14), value(1, 2, 3, 4));
        long[] time = store.computeIdealSegmentTime();
        assert time[1] == 14;
        assert time[0] == 5;
    }
}