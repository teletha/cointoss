/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import static cointoss.MarketTestSupport.*;

import org.junit.jupiter.api.Test;

import cointoss.Execution;

/**
 * @version 2018/04/23 23:31:21
 */
public class BitFlyerLogTest {

    BitFlyerLog log = new BitFlyerLog(BitFlyer.FX_BTC_JPY);

    @Test
    void compact() {
        Execution first = buy(10, 1);
        Execution second = buy(11, 2);

        String[] encoded = log.encode(second, first);
        Execution decoded = log.decode(encoded, first);
        System.out.println(first.buyer() + "  " + second.buyer() + "   " + decoded.buyer());

        assert decoded.equals(second);
    }
}
