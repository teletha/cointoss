/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.arithmeric;

import org.junit.jupiter.api.Test;

import cointoss.util.arithmeric.Primitives;

class PrimitivesTest {

    @Test
    void ratio() {
        assert Primitives.ratio(10, 20) == 0.5;
        assert Primitives.ratio(30, 100) == 0.3;
        assert Primitives.ratio(33.34, 100) == 0.333;
        assert Primitives.ratio(33.35, 100) == 0.334;
        assert Primitives.ratio(33.3444444445, 100) == 0.333;

        assert Primitives.ratio(0, 100) == 0;
        assert Primitives.ratio(0, 0) == 0;
        assert Primitives.ratio(100, 100) == 1;
        assert Primitives.ratio(100, 0) == 0;
    }

    @Test
    void percent() {
        assert Primitives.percent(10, 20).equals("50%");
        assert Primitives.percent(30, 100).equals("30%");
        assert Primitives.percent(33.34, 100).equals("33.3%");

        assert Primitives.percent(0, 100).equals("0%");
        assert Primitives.percent(0, 0).equals("0%");
        assert Primitives.percent(100, 100).equals("100%");
        assert Primitives.percent(100, 0).equals("0%");
    }
}