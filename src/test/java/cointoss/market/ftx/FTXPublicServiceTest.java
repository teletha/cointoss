/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.ftx;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;

class FTXPublicServiceTest extends PublicServiceTestTemplate<FTXService> {

    private void stopExecutionLogCollection() {
        httpClient.onGet("https://ftx.com/api/markets/BTC-PERP/trades")
                .withParameter("limit", "200")
                .withParameter("start_time", "1")
                .withParameter("end_time", Matchers.not("2"))
                .doReturn("""
                        {
                          "result": [
                          ],
                          "success": true
                        }
                        """);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTXService constructMarketService() {
        return construct(FTXService::new, FTX.BTC_PERP.marketName, FTX.BTC_PERP.setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executions() {
        httpClient.onGet("https://ftx.com/api/markets/BTC-PERP/trades?limit=200&start_time=1&end_time=2").doReturnJSON("""
                {
                  "result": [
                    {
                      "id": 67425873,
                      "liquidation": false,
                      "price": 9540.0,
                      "side": "sell",
                      "size": 0.001,
                      "time": "2020-07-23T00:46:17.838284+00:00"
                    },
                    {
                      "id": 67425812,
                      "liquidation": false,
                      "price": 9540.5,
                      "side": "buy",
                      "size": 0.0004,
                      "time": "2020-07-23T00:46:01.638901+00:00"
                    }
                  ]
                }
                """);
        stopExecutionLogCollection();

        List<Execution> list = service.executions(1, 2000).toList();
        Execution e = list.get(0);
        assert e.direction == Direction.BUY;
        assert e.price.is(9540.5);
        assert e.size.is(0.0004);
        assert e.date.isEqual(Chrono.utc(2020, 7, 23, 0, 46, 1, 638, 901));
        assert e.buyer.equals("67425812");
        assert e.delay == Execution.DelayInestimable;
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.direction == Direction.SELL;
        assert e.price.is(9540);
        assert e.size.is(0.001);
        assert e.date.isEqual(Chrono.utc(2020, 7, 23, 0, 46, 17, 838, 284));
        assert e.buyer.equals("67425873");
        assert e.delay == Execution.DelayInestimable;
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        httpClient.onGet("https://ftx.com/api/markets/BTC-PERP/trades?limit=1").doReturnJSON("""
                {
                  "result": [
                    {
                      "id": 85015816,
                      "liquidation": false,
                      "price": 11910.0,
                      "side": "sell",
                      "size": 0.0004,
                      "time": "2020-08-15T07:48:11.091799+00:00"
                    }
                  ],
                  "success": true
                }
                """);

        Execution e = service.executionLatest().to().exact();
        assert e.id == 1597477691000L;
        assert e.direction == Direction.SELL;
        assert e.price.is(11910);
        assert e.size.is(0.0004);
        assert e.date.isEqual(Chrono.utc(2020, 8, 15, 7, 48, 11, 91, 799));
        assert e.buyer.equals("85015816");
        assert e.delay == Execution.DelayInestimable;
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        websocketServer.replyWhenJSON("{'channel':'trades','market':'BTC-PERP','op':'subscribe'}", server -> {
            server.sendJSON("{'type': 'subscribed', 'channel': 'trades', 'market': 'BTC-PERP'}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85016992, 'price': 11902.5, 'size': 0.0062, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016993, 'price': 11902.5, 'size': 0.0022, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016994, 'price': 11902.5, 'size': 0.001, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016995, 'price': 11902.0, 'size': 0.0021, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016996, 'price': 11901.5, 'size': 0.1567, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85017027, 'price': 11901.5, 'size': 0.9274, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}, {'id': 85017028, 'price': 11901.0, 'size': 0.02, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85017029, 'price': 11900.5, 'size': 0.004, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:20.147541+00:00'}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 8;

        Execution exe = list.get(0);
        assert exe.id == 1597477876000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11902.5);
        assert exe.size.is(0.0062);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 16, 518, 762));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85016992");

        exe = list.get(7);
        assert exe.id == 1597477880000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11900.5);
        assert exe.size.is(0.004);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 20, 147, 541));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85017029");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        websocketServer.replyWhenJSON("{'channel':'trades','market':'BTC-PERP','op':'subscribe'}", server -> {
            server.sendJSON("{'type': 'subscribed', 'channel': 'trades', 'market': 'BTC-PERP'}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85022383, 'price': 11922.0, 'size': 0.2, 'side': 'buy', 'liquidation': false, 'time': '2020-08-15T08:02:27.515786+00:00'}, {'id': 85022384, 'price': 11922.0, 'size': 0.0028, 'side': 'buy', 'liquidation': false, 'time': '2020-08-15T08:02:27.515786+00:00'}, {'id': 85022385, 'price': 11922.0, 'size': 0.0019, 'side': 'buy', 'liquidation': false, 'time': '2020-08-15T08:02:27.515786+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85022387, 'price': 11923.5, 'size': 0.0055, 'side': 'buy', 'liquidation': false, 'time': '2020-08-15T08:02:27.837360+00:00'}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 4;

        Execution exe = list.get(0);
        assert exe.id == 1597478547000L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(11922);
        assert exe.size.is(0.2);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 8, 2, 27, 515, 786));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85022383");

        exe = list.get(1);
        assert exe.id == 1597478547001L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(11922);
        assert exe.size.is(0.0028);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 8, 2, 27, 515, 786));
        assert exe.consecutive == Execution.ConsecutiveSameBuyer;
        assert exe.buyer.equals("85022384");

        exe = list.get(2);
        assert exe.id == 1597478547002L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(11922);
        assert exe.size.is(0.0019);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 8, 2, 27, 515, 786));
        assert exe.consecutive == Execution.ConsecutiveSameBuyer;
        assert exe.buyer.equals("85022385");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        websocketServer.replyWhenJSON("{'channel':'trades','market':'BTC-PERP','op':'subscribe'}", server -> {
            server.sendJSON("{'type': 'subscribed', 'channel': 'trades', 'market': 'BTC-PERP'}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85016992, 'price': 11902.5, 'size': 0.0062, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016993, 'price': 11902.5, 'size': 0.0022, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016994, 'price': 11902.5, 'size': 0.001, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016995, 'price': 11902.0, 'size': 0.0021, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016996, 'price': 11901.5, 'size': 0.1567, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85017027, 'price': 11901.5, 'size': 0.9274, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}, {'id': 85017028, 'price': 11901.0, 'size': 0.02, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 7;

        Execution exe = list.get(0);
        assert exe.id == 1597477876000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11902.5);
        assert exe.size.is(0.0062);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 16, 518, 762));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85016992");

        exe = list.get(1);
        assert exe.id == 1597477876001L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11902.5);
        assert exe.size.is(0.0022);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 16, 518, 762));
        assert exe.consecutive == Execution.ConsecutiveSameSeller;
        assert exe.buyer.equals("85016993");

        exe = list.get(2);
        assert exe.id == 1597477876002L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11902.5);
        assert exe.size.is(0.001);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 16, 518, 762));
        assert exe.consecutive == Execution.ConsecutiveSameSeller;
        assert exe.buyer.equals("85016994");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        websocketServer.replyWhenJSON("{'channel':'trades','market':'BTC-PERP','op':'subscribe'}", server -> {
            server.sendJSON("{'type': 'subscribed', 'channel': 'trades', 'market': 'BTC-PERP'}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85016992, 'price': 11902.5, 'size': 0.0062, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016993, 'price': 11902.5, 'size': 0.0022, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016994, 'price': 11902.5, 'size': 0.001, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016995, 'price': 11902.0, 'size': 0.0021, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}, {'id': 85016996, 'price': 11901.5, 'size': 0.1567, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:16.518762+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'ETH-PERP', 'type': 'update', 'data': [{'id': 85017027, 'price': 11901.5, 'size': 0.9274, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}, {'id': 85017028, 'price': 11901.0, 'size': 0.02, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:19.938929+00:00'}]}");
            server.sendJSON("{'channel': 'trades', 'market': 'BTC-PERP', 'type': 'update', 'data': [{'id': 85017029, 'price': 11900.5, 'size': 0.004, 'side': 'sell', 'liquidation': false, 'time': '2020-08-15T07:51:20.147541+00:00'}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 6;

        Execution exe = list.get(0);
        assert exe.id == 1597477876000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11902.5);
        assert exe.size.is(0.0062);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 16, 518, 762));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85016992");

        exe = list.get(5);
        assert exe.id == 1597477880000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(11900.5);
        assert exe.size.is(0.004);
        assert exe.date.isEqual(Chrono.utc(2020, 8, 15, 7, 51, 20, 147, 541));
        assert exe.consecutive == Execution.ConsecutiveDifference;
        assert exe.buyer.equals("85017029");
    }
}
