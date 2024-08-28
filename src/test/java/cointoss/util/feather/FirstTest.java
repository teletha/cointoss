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

class FirstTest extends FeatherStoreTestBase {

    @Test
    void first() {
        FeatherStore<Value> store = createStore(Span.Minute1);
        store.store(value(300));
        assert store.first().item == 300;

        store.store(value(360));
        assert store.first().item == 300;

        store.store(value(180));
        assert store.first().item == 180;

        store.store(value(204));
        assert store.first().item == 204;

        store.store(value(156));
        assert store.first().item == 156;

        store.store(value(120));
        assert store.first().item == 120;
    }

    @Test
    void firstOverDays() {
        FeatherStore<Value> store = createStore(Span.Day);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.first().item == 0;
    }

    @Test
    void diskOnly() {
        FeatherStore<Value> store = createStore(1, 10, null, value(1, 2, 3, 4));
        assert store.first().item == 1;
    }

    @Test
    void memoryAndDisk() {
        FeatherStore<Value> store = createStore(1, 10, value(1, 2, 3, 4), value(1, 2, 3, 4));
        assert store.first().item == 1;
    }

    @Test
    void memoryHasOlderData() {
        FeatherStore<Value> store = createStore(1, 10, value(3, 4, 5), value(4, 5, 6));
        assert store.existOnHeap(value(3));
        assert store.first().item == 3;
    }

    @Test
    void diskHasOlderData() {
        FeatherStore<Value> store = createStore(1, 10, value(4, 5, 6), value(3, 4, 5));
        assert store.existOnHeap(value(3)) == false;
        assert store.first().item == 3;
    }

}