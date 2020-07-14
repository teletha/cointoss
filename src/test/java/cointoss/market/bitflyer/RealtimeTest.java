/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.execution.Execution;

class RealtimeTest {

    static WebSocketServer server = new WebSocketServer();

    WebSocketClient client = server.websocketClient();

    @BeforeAll
    static void setup() {
        BitFlyerService.Realtime.enableDebug(server.httpClient());
    }

    @AfterAll
    static void cleanup() {
        BitFlyerService.Realtime.enableDebug(null);
    }

    @Test
    void execution() {
        server.replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", () -> {
            server.sendJSON("{'jsonrpc':'2.0','id':123,'result':true}");
            server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
            server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061603-372561','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
        });

        List<Execution> list = BitFlyer.FX_BTC_JPY.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).id == 1826991347L;
        assert list.get(1).id == 1826991348L;
    }

    @Test
    void executionFail() {
        server.replyWhenJSON("{'id':123,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", () -> {
            server.sendJSON("{'jsonrpc':'2.0','id':123,'result':false}");
        });
        server.replyWhenJSON("{'id':124,'jsonrpc':'2.0','method':'subscribe','params':{'channel':'lightning_executions_FX_BTC_JPY'}}", () -> {
            server.sendJSON("{'jsonrpc':'2.0','id':124,'result':true}");
            server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991347,'side':'BUY','price':999469.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.307631Z','buy_child_order_acceptance_id':'JRF20200712-061604-686433','sell_child_order_acceptance_id':'JRF20200712-061604-026331'}]}}");
            server.sendJSON("{'jsonrpc':'2.0','method':'channelMessage','params':{'channel':'lightning_executions_FX_BTC_JPY','message':[{'id':1826991348,'side':'SELL','price':999467.0,'size':0.01,'exec_date':'2020-07-12T06:16:04.3243532Z','buy_child_order_acceptance_id':'JRF20200712-061603-372561','sell_child_order_acceptance_id':'JRF20200712-061604-575165'}]}}");
        });

        List<Execution> list = BitFlyer.FX_BTC_JPY.executionsRealtimely().toList();
        assert list.size() == 2;
        assert list.get(0).id == 1826991347L;
        assert list.get(1).id == 1826991348L;
    }
}
