/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.junit.jupiter.api.Test;

class PrimitivesTest {

    @Test
    void ratio() {
        assert Primitives.ratio(10, 20) == 50;
        assert Primitives.ratio(30, 100) == 30;
        assert Primitives.ratio(33.34, 100) == 33.3;
        assert Primitives.ratio(33.35, 100) == 33.4;
        assert Primitives.ratio(33.3444444445, 100) == 33.3;

        assert Primitives.ratio(0, 100) == 0;
        assert Primitives.ratio(0, 0) == 0;
        assert Primitives.ratio(100, 100) == 100;
        assert Primitives.ratio(100, 0) == 0;
    }
}
