package cointoss;
/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.Test;

import cointoss.market.bitflyer.BitFlyer;

/**
 * @version 2018/04/23 23:31:21
 */
public class BitFlyerLogTest {

    MarketBackend service = BitFlyer.FX_BTC_JPY.service();

    @Test
    void compact() {
        Execution first = buy(10, 1);
        Execution second = buy(11, 2);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        System.out.println(first.buyer() + "  " + second.buyer() + "   " + decoded.buyer());

        assert decoded.equals(second);
    }
}
