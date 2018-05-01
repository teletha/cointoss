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
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Execution;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyerService.BitFlyerExecution;
import cointoss.order.Order;
import cointoss.util.Chrono;

/**
 * @version 2018/04/26 10:38:43
 */
public class BitFlyerServiceTest {

    MockBitFlyerService service = new MockBitFlyerService();

    @Test
    void compactSameDelay() {
        Execution first = buy(10, 1).delay(5);
        Execution second = buy(10, 1).delay(5);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactGreaterDelay() {
        Execution first = buy(10, 1).delay(5);
        Execution second = buy(10, 1).delay(7);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactLesserDelay() {
        Execution first = buy(10, 1).delay(5);
        Execution second = buy(10, 1).delay(3);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactSameConsecutiveType() {
        Execution first = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);
        Execution second = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void compactDiffConsecutiveType() {
        Execution first = buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer);
        Execution second = buy(10, 1).consecutive(Execution.ConsecutiveDifference);

        String[] encoded = service.encode(second, first);
        Execution decoded = service.decode(encoded, first);
        assert decoded.equals(second);
    }

    @Test
    void normalize() {
        assert BitFlyerService.normalize("2018-04-26T00:32:26.1234567Z").equals("2018-04-26T00:32:26.123");
        assert BitFlyerService.normalize("2018-04-26T00:32:26.19Z").equals("2018-04-26T00:32:26.190");
    }

    @Test
    void estimateDelay() {
        BitFlyerExecution exe = new BitFlyerExecution();
        exe.side = Side.SELL;
        exe.exec_date = ZonedDateTime.of(2018, 4, 27, 9, 0, 6, 500000000, Chrono.UTC);

        // user order id
        exe.sell_child_order_acceptance_id = "JRF20180427-180006-597220";
        assert BitFlyerService.estimateDelay(exe) == 1;

        // illegal id
        exe.sell_child_order_acceptance_id = "ABCDEFGHIJKLMN";
        assert BitFlyerService.estimateDelay(exe) == 0;

        // server order id
        exe.sell_child_order_acceptance_id = "JRF20180427-090006-597220";
        assert BitFlyerService.estimateDelay(exe) == -2;
    }

    @Test
    void createPositionWhenOrderIsExecuted() {
        List<Execution> executions = service.executions().toList();
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
