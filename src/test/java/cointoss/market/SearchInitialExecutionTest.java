/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.execution.Execution;
import cointoss.market.bitmex.BitMex;

class SearchInitialExecutionTest {

    @Test
    void bitmex() {
        Execution initial = BitMex.XBT_USD.searchInitialExecution().waitForTerminate().to().exact();
        assert initial.id == 144318446570600000L;
    }
}
