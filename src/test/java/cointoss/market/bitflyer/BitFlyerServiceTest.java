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
import cointoss.Position;
import cointoss.Side;
import cointoss.order.Order;
import cointoss.util.Chrono;

/**
 * @version 2018/04/26 10:38:43
 */
public class BitFlyerServiceTest {

    MockBitFlyerService service = new MockBitFlyerService();

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
        assert BitFlyerService.normalize("2018-04-26T00:32:26.1234567Z").equals("2018-04-26T00:32:26.123");
        assert BitFlyerService.normalize("2018-04-26T00:32:26.19Z").equals("2018-04-26T00:32:26.190");
    }

    @Test
    void estimateDelay() {
        Execution exe = sell(10, 1);
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
        List<Position> positions = service.positions().toList();
        assert executions.isEmpty();
        assert positions.isEmpty();

        service.nextRequest("ServerAcceptanceID");
        assert service.request(Order.limitLong(1, 10)).to().is("ServerAcceptanceID");

        // irrelevant execution
        service.nextExecution(execution(Side.BUY, 10, 1), "DisrelatedBuyer", "DisrelatedSeller");
        assert executions.size() == 1;
        assert positions.isEmpty();

        // my execution
        service.nextExecution(execution(Side.BUY, 10, 1), "ServerAcceptanceID", "DisrelatedSeller");
        assert executions.size() == 2;
        assert positions.size() == 1;
    }
}
