/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.backtest;

import org.junit.jupiter.api.Test;

import cointoss.Trader;
import cointoss.market.bitflyer.BitFlyer;

/**
 * @version 2018/04/29 14:35:43
 */
class BackTestTest {

    @Test
    void testName() {
        BackTest test = new BackTest().log(BitFlyer.FX_BTC_JPY.log().rangeRandom(1)).currency(100000, 0).strategy(() -> new NOP()).trial(1);
        // test.run();
    }

    /**
     * @version 2018/04/29 14:43:59
     */
    private static class NOP extends Trader {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            market.timeline.to(v -> {
                System.out.println(v);
            });
        }
    }
}
