/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;
/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */

import org.junit.jupiter.api.Test;

import cointoss.MarketService;

public class BitMexServiceTest {

    @Test
    void parse() throws InterruptedException {
        MarketService service = BitMex.XBT_USD;
        service.executions(100, 600).to(e -> {
            System.out.println(e);
        });

        Thread.sleep(10000);
    }
}
