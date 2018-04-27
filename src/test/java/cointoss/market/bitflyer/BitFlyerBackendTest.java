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

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import cointoss.Execution;
import cointoss.util.Chrono;

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

    @Test
    void estimateDelay() {
        Execution exe = sell(10, 1);
        exe.exec_date = ZonedDateTime.of(2018, 4, 27, 9, 0, 6, 500000000, Chrono.UTC);

        // user order id
        exe.sell_child_order_acceptance_id = "JRF20180427-180006-597220";
        assert BitFlyerBackend.estimateDelay(exe) == 1;

        // illegal id
        exe.sell_child_order_acceptance_id = "ABCDEFGHIJKLMN";
        assert BitFlyerBackend.estimateDelay(exe) == 0;

        // server order id
        exe.sell_child_order_acceptance_id = "JRF20180427-090006-597220";
        assert BitFlyerBackend.estimateDelay(exe) == -2;
    }
}
