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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.market.MarketServiceTestBase;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Chrono;

public class BitFlyerServiceTest extends MarketServiceTestBase {

    @Test
    void parseVariousDateTimeFormat() {
        assert BitFlyerService.parse("2018-04-26T00:32:26.1234567Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.123"));
        assert BitFlyerService.parse("2018-04-26T00:32:26.19Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.190"));
        assert BitFlyerService.parse("2018-07-09T01:16:20Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:20.000"));
        assert BitFlyerService.parse("2018-07-09T01:16Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:00.000"));
        assert BitFlyerService.parse("2018-07-09T01Z").isEqual(LocalDateTime.parse("2018-07-09T01:00:00.000"));
    }

    @Override
    @Test
    protected void orderActive() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 0,
                    "child_order_id": "JFX20200714-015807-586848F",
                    "product_code": "FX_BTC_JPY",
                    "side": "SELL",
                    "child_order_type": "LIMIT",
                    "price": 1096329.0,
                    "average_price": 0.0,
                    "size": 0.01,
                    "child_order_state": "ACTIVE",
                    "expire_date": "2020-08-13T01:58:06",
                    "child_order_date": "2020-07-14T01:58:06",
                    "child_order_acceptance_id": "JRF20200714-015806-840451",
                    "outstanding_size": 0.01,
                    "cancel_size": 0.0,
                    "executed_size": 0.0,
                    "total_commission": 0.0
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.ACTIVE).toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200714-015806-840451");
        assert order.direction.isSell();
        assert order.type.isMaker();
        assert order.size.is(0.01);
        assert order.remainingSize.is(0.01);
        assert order.executedSize.is(0);
        assert order.canceledSize().is(0);
        assert order.price.is(1096329);
        assert order.creationTime.isEqual(ZonedDateTime.of(2020, 7, 14, 1, 58, 6, 0, Chrono.UTC));
        assert order.isActive();
        assert order.isNotCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isNotTerminated();
    }

    @Test
    void orderActiveEmpty() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.ACTIVE).toList();
        assert list.size() == 0;
    }

    @Test
    void orderCanceled() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "CANCELED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.200000000000,
                    "executed_size": 0.300000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.CANCELED).toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.direction.isBuy();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize.is(0);
        assert order.executedSize.is(0.3);
        assert order.canceledSize().is(0.2);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(ZonedDateTime.of(2020, 7, 10, 9, 1, 43, 0, Chrono.UTC));
        assert order.isNotActive();
        assert order.isCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    @Test
    void orderCanceledEmpty() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.CANCELED).toList();
        assert list.size() == 0;
    }

    @Test
    void orderCompleted() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "COMPLETED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.000000000000,
                    "executed_size": 0.500000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.COMPLETED).toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.direction.isBuy();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize.is(0);
        assert order.executedSize.is(0.5);
        assert order.canceledSize().is(0);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(ZonedDateTime.of(2020, 7, 10, 9, 1, 43, 0, Chrono.UTC));
        assert order.isNotActive();
        assert order.isNotCanceled();
        assert order.isCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    @Test
    void orderCompletedEmpty() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.COMPLETED).toList();
        assert list.size() == 0;
    }

    @Test
    void orders() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 0,
                    "child_order_id": "JFX20200714-015807-586848F",
                    "product_code": "FX_BTC_JPY",
                    "side": "SELL",
                    "child_order_type": "LIMIT",
                    "price": 1096329.0,
                    "average_price": 0.0,
                    "size": 0.01,
                    "child_order_state": "ACTIVE",
                    "expire_date": "2020-08-13T01:58:06",
                    "child_order_date": "2020-07-14T01:58:06",
                    "child_order_acceptance_id": "JRF20200714-015806-840451",
                    "outstanding_size": 0.01,
                    "cancel_size": 0.0,
                    "executed_size": 0.0,
                    "total_commission": 0.0
                  },
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "COMPLETED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.000000000000,
                    "executed_size": 0.500000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders().toList();
        assert list.size() == 2;

        Order order = list.get(0);
        assert order.id.equals("JRF20200714-015806-840451");
        assert order.direction.isSell();
        assert order.type.isMaker();
        assert order.size.is(0.01);
        assert order.remainingSize.is(0.01);
        assert order.executedSize.is(0);
        assert order.canceledSize().is(0);
        assert order.price.is(1096329);
        assert order.creationTime.isEqual(ZonedDateTime.of(2020, 7, 14, 1, 58, 6, 0, Chrono.UTC));
        assert order.isActive();
        assert order.isNotCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isNotTerminated();

        order = list.get(1);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.direction.isBuy();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize.is(0);
        assert order.executedSize.is(0.5);
        assert order.canceledSize().is(0);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(ZonedDateTime.of(2020, 7, 10, 9, 1, 43, 0, Chrono.UTC));
        assert order.isNotActive();
        assert order.isNotCanceled();
        assert order.isCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    @Test
    void ordersEmpty() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders().toList();
        assert list.size() == 0;
    }

    @Test
    void executions() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                    {
                        "id": 1828074165,
                        "side": "BUY",
                        "price": 999231,
                        "size": 0.01,
                        "exec_date": "2020-07-13T07:41:43.097",
                        "buy_child_order_acceptance_id": "JRF20200713-074142-266150",
                        "sell_child_order_acceptance_id": "JRF20200713-074142-697549"
                    },
                    {
                        "id": 1828074164,
                        "side": "BUY",
                        "price": 999224,
                        "size": 0.1,
                        "exec_date": "2020-07-13T07:41:43.097",
                        "buy_child_order_acceptance_id": "JRF20200713-074142-266150",
                        "sell_child_order_acceptance_id": "JRF20200713-074142-809298"
                    }
                ]
                """);

        List<Execution> list = service.executions(1, 10).toList();
        Execution e = list.get(0);
        assert e.id == 1828074164;
        assert e.direction == Direction.BUY;
        assert e.price.is(999224);
        assert e.size.is(0.1);
        assert e.date.isEqual(ZonedDateTime.of(2020, 7, 13, 7, 41, 43, 97000000, Chrono.UTC));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-809298");
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id == 1828074165;
        assert e.direction == Direction.BUY;
        assert e.price.is(999231);
        assert e.size.is(0.01);
        assert e.date.isEqual(ZonedDateTime.of(2020, 7, 13, 7, 41, 43, 97000000, Chrono.UTC));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-697549");
        assert e.consecutive == Execution.ConsecutiveSameBuyer;
    }

    @Test
    void executionLatest() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.httpClient.onGet().doReturnJSON("""
                [
                    {
                        "id": 1828011727,
                        "side": "BUY",
                        "price": 999262,
                        "size": 0.03954578,
                        "exec_date": "2020-07-13T06:24:54.157",
                        "buy_child_order_acceptance_id": "JRF20200713-062454-244956",
                        "sell_child_order_acceptance_id": "JRF20200713-062452-031817"
                    }
                ]
                """);

        Execution e = service.executionLatest().to().exact();
        assert e.id == 1828011727;
        assert e.direction == Direction.BUY;
        assert e.price.is(999262);
        assert e.size.is(0.03954578);
        assert e.date.isEqual(ZonedDateTime.of(2020, 7, 13, 6, 24, 54, 157000000, Chrono.UTC));
        assert e.buyer.equals("JRF20200713-062454-244956");
        assert e.seller.equals("JRF20200713-062452-031817");
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    @Test
    void executionRealtimely() {
        BitFlyerServiceMock service = new BitFlyerServiceMock();
        service.websocketServer
                .replyWhenJSON("{'id':1,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':1,'result':true}");
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
        BitFlyerServiceMock service = new BitFlyerServiceMock();
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
        BitFlyerServiceMock service = new BitFlyerServiceMock();
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
        BitFlyerServiceMock service = new BitFlyerServiceMock();
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