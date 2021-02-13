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

import cointoss.ticker.Span;
import cointoss.util.Chrono;
import cointoss.util.feather.FeatherStoreTest.Value;
import psychopath.Locator;

public class AA {

    public static void main(String[] args) {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDiskStore(Locator.file("test").asJavaPath());
        store.store(new Value(Chrono.currentTimeMills() / 1000));
        store.commit();
    }
}
