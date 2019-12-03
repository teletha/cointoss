/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.trading.LazyBear;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .start(2019, 11, 8)
                .end(2019, 11, 8)
                .traders(new LazyBear())
                .initialBaseCurrency(3000000)
                .detail(true)
                .fastLog(true)
                .run();
    }
}
