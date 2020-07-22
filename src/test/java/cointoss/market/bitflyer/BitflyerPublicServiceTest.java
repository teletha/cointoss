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
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;

class BitflyerPublicServiceTest extends PublicServiceTestTemplate<BitFlyerService> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected BitFlyerService constructMarketService() {
        return construct(BitFlyerService::new, BitFlyer.FX_BTC_JPY.marketName, BitFlyer.FX_BTC_JPY.setting, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executions() {
        httpClient.onGet().doReturnJSON("""
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
        assert e.date.isEqual(Chrono.utc(2020, 7, 13, 7, 41, 43, 97));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-809298");
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id == 1828074165;
        assert e.direction == Direction.BUY;
        assert e.price.is(999231);
        assert e.size.is(0.01);
        assert e.date.isEqual(Chrono.utc(2020, 7, 13, 7, 41, 43, 97));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-697549");
        assert e.consecutive == Execution.ConsecutiveSameBuyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionLatest() {
        httpClient.onGet().doReturnJSON("""
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
        assert e.date.isEqual(Chrono.utc(2020, 7, 13, 6, 24, 54, 157));
        assert e.buyer.equals("JRF20200713-062454-244956");
        assert e.seller.equals("JRF20200713-062452-031817");
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimely() {
        websocketServer
                .replyWhenJSON("{'id':(\\d+),'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':$1,'result':true}");
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
        assert exe.date.isEqual(Chrono.utc(2020, 7, 12, 6, 16, 4, 307));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("JRF20200712-061604-686433");
        assert exe.seller.equals("JRF20200712-061604-026331");

        exe = list.get(1);
        assert exe.id == 1826991348L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(999467.0);
        assert exe.size.is(0.1);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 12, 6, 16, 4, 324));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("JRF20200712-061603-372561");
        assert exe.seller.equals("JRF20200712-061604-575165");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveBuy() {
        websocketServer
                .replyWhenJSON("{'id':(\\d+),'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':$1,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'BUY','price':999467.0,'size':0.1,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).consecutive == Execution.ConsecutiveDifference;
        assert list.get(1).consecutive == Execution.ConsecutiveSameBuyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyConsecutiveSell() {
        websocketServer
                .replyWhenJSON("{'id':(\\d+),'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':$1,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'SELL','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-590195-152395','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.1,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).consecutive == Execution.ConsecutiveDifference;
        assert list.get(1).consecutive == Execution.ConsecutiveSameSeller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    protected void executionRealtimelyWithMultipleChannels() {
        websocketServer
                .replyWhenJSON("{'id':(\\d+),'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", server -> {
                    server.sendJSON("{'jsonrpc':'2.0','id':$1,'result':true}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_IGNORED','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
                    server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061603-372561','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 1;
    }

    @Test
    protected void parseVariousDateTimeFormat() {
        assert BitFlyerService.parse("2018-04-26T00:32:26.1234567Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.123"));
        assert BitFlyerService.parse("2018-04-26T00:32:26.19Z").isEqual(LocalDateTime.parse("2018-04-26T00:32:26.190"));
        assert BitFlyerService.parse("2018-07-09T01:16:20Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:20.000"));
        assert BitFlyerService.parse("2018-07-09T01:16Z").isEqual(LocalDateTime.parse("2018-07-09T01:16:00.000"));
        assert BitFlyerService.parse("2018-07-09T01Z").isEqual(LocalDateTime.parse("2018-07-09T01:00:00.000"));
    }
}