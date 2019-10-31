/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
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

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.order.Order;

public class BitFlyerServiceTest {

    MockBitFlyerService service = new MockBitFlyerService();

    @Test
    void parse() {
        assert BitFlyerService.parse("2018-04-26T00:32:26.1234567Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.123"));
        assert BitFlyerService.parse("2018-04-26T00:32:26.19Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.190"));
        assert BitFlyerService.parse("2018-07-09T01:16:20Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:20.000"));
        assert BitFlyerService.parse("2018-07-09T01:16Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:00.000"));
        assert BitFlyerService.parse("2018-07-09T01Z").isEqual(LocalDateTime.parse("2018-07-09T01:00:00.000"));
    }

    @Test
    void order() {
        service.ordersWillResponse(Order.with.buy(1).price(10), "FirstOrder");
        List<Order> orders = service.orders().toList();
        assert orders.size() == 1;
    }
}
