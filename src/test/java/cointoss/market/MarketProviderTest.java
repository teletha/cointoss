/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

/**
 * @version 2018/07/26 23:49:05
 */
class MarketProviderTest {

    static {
        // dirty code to load extensions
        assert Num.ZERO != null;
    }

    @Test
    void provider() {
        assert MarketProvider.availableProviders().toList().isEmpty() == false;
    }
}
