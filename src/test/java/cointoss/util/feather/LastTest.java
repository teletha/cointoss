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

import cointoss.ticker.Span;

class LastTest extends FeatherStoreTestBase {

    @Test
    void last() {
        FeatherStore<Value> store = createStore(Span.Minute1);
        store.store(value(60));
        assert store.last().value == 60;

        store.store(value(120));
        assert store.last().value == 120;

        store.store(value(144));
        assert store.last().value == 144;

        store.store(value(240));
        assert store.last().value == 240;

        store.store(value(192));
        assert store.last().value == 240;

        store.store(value(360));
        assert store.last().value == 360;
    }

    @Test
    void lastOverDays() {
        FeatherStore<Value> store = createStore(Span.Day);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.last().value == 4 * days;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        assert store.last().value == 4;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), value(1, 2, 3, 4));
        assert store.last().value == 4;
    }

    @Test
    void memoryHasLatestData() {
        FeatherStore<Value> store = createStore(1, 10, value(3, 4, 5), value(1, 2, 3, 4));
        assert store.existOnHeap(value(5));
        assert store.last().value == 5;
    }

    @Test
    void diskHasLatestData() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3), value(3, 4, 5));
        assert store.existOnHeap(value(5)) == false;
        assert store.last().value == 5;
    }
}