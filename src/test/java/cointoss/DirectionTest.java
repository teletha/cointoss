/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import org.junit.jupiter.api.Test;

class DirectionTest {

    @Test
    void parse() {
        assert Direction.parse("buy") == Direction.BUY;
        assert Direction.parse("BUY") == Direction.BUY;
        assert Direction.parse("sell") == Direction.SELL;
        assert Direction.parse("SELL") == Direction.SELL;
    }
}