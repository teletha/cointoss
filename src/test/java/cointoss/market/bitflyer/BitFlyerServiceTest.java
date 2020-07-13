/*
 * Copyright (C) 2020 cointoss Development Team
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
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.util.Chrono;

public class BitFlyerServiceTest {

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
        MockBitFlyerService service = new MockBitFlyerService();

        service.ordersWillResponse(Order.with.buy(1).price(10), "FirstOrder");
        List<Order> orders = service.orders().toList();
        assert orders.size() == 1;
    }

    @Test
    void executionRealtimely() {
        MockBitFlyerService service = new MockBitFlyerService();
        service.websocketServer
                .replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':123,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.1,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061603-372561','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;

        Execution exe = list.get(0);
        assert exe.id == 1826991347L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(999469.0);
        assert exe.size.is(0.01);
        assert exe.date.isEqual(ZonedDateTime.of(2020, 7, 12, 6, 16, 4, 307000000, Chrono.UTC));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("JRF20200712-061604-686433");
        assert exe.seller.equals("JRF20200712-061604-026331");

        exe = list.get(1);
        assert exe.id == 1826991348L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(999467.0);
        assert exe.size.is(0.1);
        assert exe.date.isEqual(ZonedDateTime.of(2020, 7, 12, 6, 16, 4, 324000000, Chrono.UTC));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("JRF20200712-061603-372561");
        assert exe.seller.equals("JRF20200712-061604-575165");
    }

    @Test
    void executionRealtimelyConsecutiveBuy() {
        MockBitFlyerService service = new MockBitFlyerService();
        service.websocketServer
                .replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':123,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'BUY','price':999467.0,'size':0.1,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).consecutive == Execution.ConsecutiveDifference;
        assert list.get(1).consecutive == Execution.ConsecutiveSameBuyer;
    }

    @Test
    void executionRealtimelyConsecutiveSell() {
        MockBitFlyerService service = new MockBitFlyerService();
        service.websocketServer
                .replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':123,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'SELL','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-590195-152395','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.1,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).consecutive == Execution.ConsecutiveDifference;
        assert list.get(1).consecutive == Execution.ConsecutiveSameSeller;
    }

    @Test
    void executionRealtimelyWithMultipleChannels() {
        MockBitFlyerService service = new MockBitFlyerService();
        service.websocketServer
                .replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':123,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_IGNORED','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061603-372561','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 1;
    }
}