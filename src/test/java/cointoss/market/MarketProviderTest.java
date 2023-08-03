/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.Market;
import kiss.I;

class MarketProviderTest {

    static {
        I.load(Market.class);
    }

    @Test
    void provider() {
        assert MarketServiceProvider.availableProviders().toList().isEmpty() == false;
    }
}