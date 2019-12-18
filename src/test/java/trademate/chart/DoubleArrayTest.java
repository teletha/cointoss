/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart;

import org.junit.jupiter.api.Test;

class DoubleArrayTest {

    @Test
    void array() {
        DoubleArray base = new DoubleArray(10);
        base.add(0);
        base.add(1);
        double[] array = base.array();
        assert array[0] == 0;
        assert array[1] == 1;
    }
}
