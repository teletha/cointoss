package cointoss.market.bitflyer;
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

import cointoss.Execution;

/**
 * @version 2018/04/26 10:38:43
 */
public class BitFlyerBackendTest {

    BitFlyerBackend service = new BitFlyerBackend(BitFlyer.FX_BTC_JPY);

    @Test
    void compact() {
        Execution first = buy(10, 1);
        Execution second = buy(11, 2);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        System.out.println(first.buyer() + "  " + second.buyer() + "   " + decoded.buyer());

        assert decoded.equals(second);
    }

    @Test
    void normalize() {
        assert BitFlyerBackend.normalize("2018-04-26T00:32:26.1234567Z").equals("2018-04-26T00:32:26.123");
        assert BitFlyerBackend.normalize("2018-04-26T00:32:26.19Z").equals("2018-04-26T00:32:26.190");
    }
}
