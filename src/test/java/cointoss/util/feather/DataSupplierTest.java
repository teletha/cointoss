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

import org.junit.jupiter.api.Test;

import cointoss.ticker.Span;
import cointoss.util.feather.FeatherStoreTest.Value;
import kiss.Signal;
import kiss.Signaling;

class DataSupplierTest {

    Signaling<Integer> stream = new Signaling();

    Signal<Value> supplier = stream.expose.map(Value::new);

    @Test
    void passive() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDataSupplier(supplier);
        assert store.size() == 0;

        stream.accept(0);
        assert store.size() == 1;
        assert store.at(0).value == 0;

        stream.accept(60);
        assert store.size() == 2;
        assert store.at(60).value == 60;

        stream.accept(120);
        assert store.size() == 3;
        assert store.at(120).value == 120;

        stream.accept(130);
        assert store.size() == 3;
        assert store.at(120).value == 130;
    }
}
