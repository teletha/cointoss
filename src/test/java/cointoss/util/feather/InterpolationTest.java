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

class InterpolationTest extends FeatherStoreTestBase {

    @Test
    void at() {
        FeatherStore<Value> store = createStore(value(0, 1, 5, 10, 20), null).enableInterpolation(5);
        assert store.at(0).item == 0;
        assert store.at(1).item == 1;
        assert store.at(2).item == 1;
        assert store.at(3).item == 1;
        assert store.at(4).item == 1;
        assert store.at(5).item == 5;
        assert store.at(6).item == 5;
        assert store.at(7).item == 5;
        assert store.at(8).item == 5;
        assert store.at(9).item == 5;
        assert store.at(10).item == 10;
        assert store.at(11).item == 10;
        assert store.at(12).item == 10;
        assert store.at(13).item == 10;
        assert store.at(14).item == 10;
        assert store.at(15).item == 10;
        assert store.at(16) == null;
        assert store.at(17) == null;
        assert store.at(18) == null;
        assert store.at(19) == null;
        assert store.at(20).item == 20;
    }
}