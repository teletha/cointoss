/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import org.junit.jupiter.api.Test;

class RealizedProfitTest extends TradingSituation {

    @Test
    @Override
    void entryOnly() {
        assert s.realizedProfit.is(0);
    }

    @Override
    void entryExecutedPartially() {
    }

    @Test
    @Override
    void exitCompleted() {

    }
}
