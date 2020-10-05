/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;

class TrendLineTest {

    @Test
    void testName() {
        TrendLine line = new TrendLine(20);
        for (int i = 0; i < 20; i++) {
            line.add(i, i);
        }

        line.build();
    }
}
