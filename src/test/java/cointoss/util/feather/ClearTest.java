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

class ClearTest extends FeatherStoreTestBase {

    @Test
    void clearAt() {
        FeatherStore<Value> store = createStore(10, 10, value(1, 2, 3, 4, 5), null);
        assert store.size() == 5;

        store.clear(3);
        assert store.size() == 4;
    }
}