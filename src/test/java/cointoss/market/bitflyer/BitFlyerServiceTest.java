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

import cointoss.execution.Executed;
import cointoss.execution.Execution;
import cointoss.order.Order;

/**
 * @version 2018/04/26 10:38:43
 */
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
        service.ordersWillResponse(Order.buy(1).price(10), "FirstOrder");
        List<Order> orders = service.orders().toList();
        assert orders.size() == 1;
    }

    @Test
    void createPositionWhenOrderIsExecuted() {
        List<Execution> executions = service.executionsRealtimely().toList();
        List<Execution> positions = service.executionsRealtimelyForMe().toList();

        service.requestWillResponse("ServerAcceptanceID");
        assert service.request(Order.buy(1).price(10)).to().is("ServerAcceptanceID");

        // irrelevant execution
        service.executionWillResponse(Executed.buy(1).price(10), "DisrelatedBuyer", "DisrelatedSeller");
        assert executions.size() == 1;
        assert positions.size() == 0;

        // my execution
        service.executionWillResponse(Executed.sell(1).price(10), "ServerAcceptanceID", "DisrelatedSeller");
        assert executions.size() == 2;
        assert positions.size() == 1;
        assert positions.get(0).side.isBuy();
    }
}
