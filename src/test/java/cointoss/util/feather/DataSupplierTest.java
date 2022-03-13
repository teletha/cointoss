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

import java.util.function.LongFunction;

import org.junit.jupiter.api.Test;

import cointoss.ticker.Span;
import cointoss.util.feather.FeatherStoreTest.Value;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;

class DataSupplierTest {

    Signaling<Integer> stream = new Signaling();

    Signal<Value> passive = stream.expose.map(Value::new);

    LongFunction<Signal<Value>> bulk = time -> {
        return I.signal(0).recurse(i -> i + 1).take(60 * 24).map(i -> new Value(time + i * 60));
    };

    @Test
    void passive() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDataSupplier(passive);
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

    @Test
    void bulk() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableBulkDataSupplier(bulk);
        assert store.size() == 0;

        assert store.at(0).value == 0;
        assert store.at(86400 - 60).value == 86400 - 60;
        assert store.size() == 1440;

        assert store.at(86400).value == 86400;
        assert store.size() == 1440; // max size of in-memory
    }
}
