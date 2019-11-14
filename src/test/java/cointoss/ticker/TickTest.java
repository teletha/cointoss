/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;

class TickTest {

    @Test
    void typicalPrice() {
        Tick tick = TickerTestSupport.tick(Span.Second5, 10, 15, 4, 8);
        assert tick.typicalPrice().is(9);
    }

    @Test
    void medianPrice() {
        Tick tick = TickerTestSupport.tick(Span.Second5, 10, 15, 4, 8);
        assert tick.medianPrice().is(9.5);
    }
}
