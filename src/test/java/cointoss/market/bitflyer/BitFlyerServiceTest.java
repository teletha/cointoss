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

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Execution;
import cointoss.Side;
import cointoss.order.Order;

/**
 * @version 2018/04/26 10:38:43
 */
public class BitFlyerServiceTest {

    MockBitFlyerService service = new MockBitFlyerService();

    @Test
    void normalize() {
        assert BitFlyerService.normalize("2018-04-26T00:32:26.1234567Z").equals("2018-04-26T00:32:26.123");
        assert BitFlyerService.normalize("2018-04-26T00:32:26.19Z").equals("2018-04-26T00:32:26.190");
    }

    @Test
    void createPositionWhenOrderIsExecuted() {
        List<Execution> executions = service.executionsEternally().toList();
        List<Execution> positions = service.positions().toList();

        service.requestWillResponse("ServerAcceptanceID");
        assert service.request(Order.limitLong(1, 10)).to().is("ServerAcceptanceID");

        // irrelevant execution
        service.executionWillResponse(execution(Side.BUY, 10, 1), "DisrelatedBuyer", "DisrelatedSeller");
        assert executions.size() == 1;
        assert positions.size() == 0;

        // my execution
        service.executionWillResponse(execution(Side.SELL, 10, 1), "ServerAcceptanceID", "DisrelatedSeller");
        assert executions.size() == 2;
        assert positions.size() == 1;
        assert positions.get(0).side.isBuy();
    }
}
