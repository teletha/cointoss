/*
 * Copyright (C) 2023 The COINTOSS Development Team
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
        assert store.at(0).value() == 0;
        assert store.at(1).value() == 1;
        assert store.at(2).value() == 1;
        assert store.at(3).value() == 1;
        assert store.at(4).value() == 1;
        assert store.at(5).value() == 5;
        assert store.at(6).value() == 5;
        assert store.at(7).value() == 5;
        assert store.at(8).value() == 5;
        assert store.at(9).value() == 5;
        assert store.at(10).value() == 10;
        assert store.at(11).value() == 10;
        assert store.at(12).value() == 10;
        assert store.at(13).value() == 10;
        assert store.at(14).value() == 10;
        assert store.at(15).value() == 10;
        assert store.at(16) == null;
        assert store.at(17) == null;
        assert store.at(18) == null;
        assert store.at(19) == null;
        assert store.at(20).value() == 20;
    }
}