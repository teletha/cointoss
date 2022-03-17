/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitfinex;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;

class BitfinexPublicServiceTest extends PublicServiceTestTemplate<BitfinexService> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected BitfinexService constructMarketService() {
        return construct(BitfinexService::new, Bitfinex.BTC_USD.marketName, Bitfinex.BTC_USD.setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executions() {
        httpClient.onGet().doReturn("""
                [
                  [
                    4145,
                    1358182043000,
                    0.2721858,
                    14.5373664
                  ],
                  [
                    4146,
                    1358182044000,
                    103,
                    14.5373664
                  ],
                  [
                    4149,
                    1358185856000,
                    -20,
                    14.5329498
                  ]
                ]
                """);

        List<Execution> list = service.executions(1, 10).toList();
        assert list.size() == 3;

        Execution e = list.get(0);
        assert e.id == 13581820430000000L;
        assert e.direction == Direction.BUY;
        assert e.price.is(14.5373664);
        assert e.size.is(0.2721858);
        assert e.date.isEqual(Chrono.utc(2013, 1, 14, 16, 47, 23, 0));
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id == 13581820440000000L;
        assert e.direction == Direction.BUY;
        assert e.price.is(14.5373664);
        assert e.size.is(103);
        assert e.date.isEqual(Chrono.utc(2013, 1, 14, 16, 47, 24, 0));
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(2);
        assert e.id == 13581858560000000L;
        assert e.direction == Direction.SELL;
        assert e.price.is(14.5329498);
        assert e.size.is(20);
        assert e.date.isEqual(Chrono.utc(2013, 1, 14, 17, 50, 56, 0));
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        httpClient.onGet().doReturn("""
                [
                  [
                    482083220,
                    1595428699510,
                    0.0021,
                    9342.2
                  ]
                ]
                """);

        Execution e = service.executionLatest().to().exact();
        assert e.id == 15954286995100000L;
        assert e.direction == Direction.BUY;
        assert e.price.is(9342.2);
        assert e.size.is(0.0021);
        assert e.date.isEqual(Chrono.utc(2020, 7, 22, 14, 38, 19, 510));
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        websocketServer
                .replyWhenJSON("{'chanId':0,'channel':'trades','event':'subscribe','freq':'F1','key':'BTCUSD','length':'250','symbol':'BTCUSD'}", server -> {
                    server.sendJSON("{'event':'info','version':2,'serverId':'f4baee08-dc2c-4d28-b1f9-4025dd208e91','platform':{'status':1}}");
                    server.sendJSON("{'event':'subscribed','channel':'trades','chanId':53,'symbol':'tBTCUSD','pair':'BTCUSD'}");
                    server.sendJSON("[53,[[482101439,1595434632184,0.61910263,9360.5],[482101438,1595434606489,0.00327385,9360.5],[482101435,1595434604394,0.00094,9359.6109514],[482101432,1595434596383,0.00594,9359.6109514],[482101431,1595434596383,0.10676642,9359.5],[482101430,1595434587904,0.61916455,9359.5],[482101429,1595434570069,0.52006903,9359.5],[482101428,1595434570069,0.09918471,9359.5],[482101427,1595434548716,0.1,9359.5],[482101426,1595434546228,0.005,9359.5],[482101424,1595434539878,-0.02016,9359.4889272],[482101422,1595434523870,-0.05557656,9359.4889272],[482101420,1595434510249,-0.05560394,9359.4889272],[482101419,1595434490930,-0.03078213,9359.4],[482101418,1595434488727,-0.27623317,9359.4],[482101416,1595434488727,-0.00066,9359.48363747],[482101414,1595434488727,-0.00066,9359.48363747],[482101412,1595434488727,-0.00066,9359.48363747],[482101410,1595434488727,-0.00066,9359.48363747],[482101408,1595434488727,-0.00066,9359.48363747],[482101406,1595434488727,-0.00066,9359.48363747],[482101404,1595434488727,-0.00066,9359.48363747],[482101402,1595434488727,-0.00066,9359.48363747],[482101400,1595434488727,-0.00066,9359.48363747],[482101399,1595434478336,0.00066,9359.5],[482101397,1595434478336,0.00066,9359.5],[482101395,1595434478336,0.00066,9359.5],[482101393,1595434478336,0.00066,9359.5],[482101391,1595434478336,0.00066,9359.5],[482101389,1595434478336,0.00066,9359.5]]]");
                    server.sendJSON("[53,'te',[482101440,1595434640308,0.61909792,9360.5]]");
                    server.sendJSON("[53,'tu',[482101440,1595434640308,0.61909792,9360.5]]");
                    server.sendJSON("[53,'te',[482101452,1595434648522,-0.61907935,9360.5]]");
                    server.sendJSON("[53,'tu',[482101452,1595434648522,-0.61907935,9360.5]]");
                    server.sendJSON("[53,'hb']");
                    server.sendJSON("[53,'te',[482101453,1595434658037,0.61907373,9360]]");
                    server.sendJSON("[53,'tu',[482101453,1595434658037,0.61907373,9360]]");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 3;

        Execution exe = list.get(0);
        assert exe.id == 15954346403080000L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(9360.5);
        assert exe.size.is(0.61909792);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 17, 20, 308));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id == 15954346485220000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(9360.5);
        assert exe.size.is(0.61907935);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 17, 28, 522));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(2);
        assert exe.id == 15954346580370000L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(9360);
        assert exe.size.is(0.61907373);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 17, 38, 37));
        assert exe.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        websocketServer
                .replyWhenJSON("{'chanId':0,'channel':'trades','event':'subscribe','freq':'F1','key':'BTCUSD','length':'250','symbol':'BTCUSD'}", server -> {
                    server.sendJSON("{'event':'info','version':2,'serverId':'f4baee08-dc2c-4d28-b1f9-4025dd208e91','platform':{'status':1}}");
                    server.sendJSON("{'event':'subscribed','channel':'trades','chanId':41,'symbol':'tBTCUSD','pair':'BTCUSD'}");
                    server.sendJSON("[41,[[482083236,1595428778562,-0.00348216,9343.27028166],[482083234,1595428746373,0.00661892,9344.3],[482083233,1595428746373,0.00968224,9344.1],[482083231,1595428746373,0.00226506,9344.09137053],[482083230,1595428746373,0.13794872,9343.4],[482083229,1595428746373,0.01063592,9342.2],[482083228,1595428746373,0.04,9342.2],[482083227,1595428746373,0.04284914,9342.2],[482083226,1595428728960,0.00715086,9342.2],[482083225,1595428728960,0.01284914,9342.2],[482083223,1595428714671,-0.00711651,9342.1121303],[482083221,1595428711320,-0.00853146,9342.1121303],[482083220,1595428699510,0.0021,9342.2],[482083219,1595428698714,0.057986,9342.2],[482083217,1595428651782,-0.01,9342.1121303],[482083216,1595428651715,0.01718075,9342.2],[482083214,1595428651715,0.00281925,9342.18392002],[482083212,1595428639200,-0.01273494,9342.1121303],[482083210,1595428639200,-0.00226506,9342.18392002],[482083209,1595428629032,0.00535,9342.2],[482083208,1595428608678,0.19465273,9342.2],[482083207,1595428608678,0.0042,9342.2],[482083205,1595428608678,0.00114727,9342.18392002],[482083203,1595428606861,0.00111779,9342.18392002],[482083201,1595428544293,-0.29718075,9342.1121303],[482083199,1595428544293,-0.00281925,9342.18392002],[482083198,1595428510467,0.00500394,9342.2],[482083195,1595428458294,-0.22645823,9342.1121303],[482083193,1595428458294,-0.000615,9342.1121303],[482083191,1595428458294,-0.000615,9342.1121303]]]");
                    server.sendJSON("[41,'te',[482083238,1595428907203,0.05,9343.3]]");
                    server.sendJSON("[41,'te',[482083239,1595428907203,0.56969684,9343.3]]");
                    server.sendJSON("[41,'tu',[482083239,1595428907203,0.56969684,9343.3]]");
                    server.sendJSON("[41,'tu',[482083238,1595428907203,0.05,9343.3]]");
                    server.sendJSON("[41,'hb']");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;

        Execution exe = list.get(0);
        assert exe.id == 15954289072030000L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(9343.3);
        assert exe.size.is(0.05);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 14, 41, 47, 203));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id == 15954289072030001L;
        assert exe.direction == Direction.BUY;
        assert exe.price.is(9343.3);
        assert exe.size.is(0.56969684);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 14, 41, 47, 203));
        assert exe.consecutive == Execution.ConsecutiveSameBuyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        websocketServer
                .replyWhenJSON("{'chanId':0,'channel':'trades','event':'subscribe','freq':'F1','key':'BTCUSD','length':'250','symbol':'BTCUSD'}", server -> {
                    server.sendJSON("{'event':'info','version':2,'serverId':'f4baee08-dc2c-4d28-b1f9-4025dd208e91','platform':{'status':1}}");
                    server.sendJSON("{'event':'subscribed','channel':'trades','chanId':41,'symbol':'tBTCUSD','pair':'BTCUSD'}");
                    server.sendJSON("[41,[[482083236,1595428778562,-0.00348216,9343.27028166],[482083234,1595428746373,0.00661892,9344.3],[482083233,1595428746373,0.00968224,9344.1],[482083231,1595428746373,0.00226506,9344.09137053],[482083230,1595428746373,0.13794872,9343.4],[482083229,1595428746373,0.01063592,9342.2],[482083228,1595428746373,0.04,9342.2],[482083227,1595428746373,0.04284914,9342.2],[482083226,1595428728960,0.00715086,9342.2],[482083225,1595428728960,0.01284914,9342.2],[482083223,1595428714671,-0.00711651,9342.1121303],[482083221,1595428711320,-0.00853146,9342.1121303],[482083220,1595428699510,0.0021,9342.2],[482083219,1595428698714,0.057986,9342.2],[482083217,1595428651782,-0.01,9342.1121303],[482083216,1595428651715,0.01718075,9342.2],[482083214,1595428651715,0.00281925,9342.18392002],[482083212,1595428639200,-0.01273494,9342.1121303],[482083210,1595428639200,-0.00226506,9342.18392002],[482083209,1595428629032,0.00535,9342.2],[482083208,1595428608678,0.19465273,9342.2],[482083207,1595428608678,0.0042,9342.2],[482083205,1595428608678,0.00114727,9342.18392002],[482083203,1595428606861,0.00111779,9342.18392002],[482083201,1595428544293,-0.29718075,9342.1121303],[482083199,1595428544293,-0.00281925,9342.18392002],[482083198,1595428510467,0.00500394,9342.2],[482083195,1595428458294,-0.22645823,9342.1121303],[482083193,1595428458294,-0.000615,9342.1121303],[482083191,1595428458294,-0.000615,9342.1121303]]]");
                    server.sendJSON("[41,'te',[482083238,1595428907203,-0.05,9343.3]]");
                    server.sendJSON("[41,'te',[482083239,1595428907203,-0.56969684,9343.3]]");
                    server.sendJSON("[41,'tu',[482083239,1595428907203,-0.56969684,9343.3]]");
                    server.sendJSON("[41,'tu',[482083238,1595428907203,-0.05,9343.3]]");
                    server.sendJSON("[41,'hb']");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;

        Execution exe = list.get(0);
        assert exe.id == 15954289072030000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(9343.3);
        assert exe.size.is(0.05);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 14, 41, 47, 203));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id == 15954289072030001L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(9343.3);
        assert exe.size.is(0.56969684);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 14, 41, 47, 203));
        assert exe.consecutive == Execution.ConsecutiveSameSeller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        websocketServer
                .replyWhenJSON("{'chanId':0,'channel':'trades','event':'subscribe','freq':'F1','key':'BTCUSD','length':'250','symbol':'BTCUSD'}", server -> {
                    server.sendJSON("{'event':'info','version':2,'serverId':'f4baee08-dc2c-4d28-b1f9-4025dd208e91','platform':{'status':1}}");
                    server.sendJSON("{'event':'subscribed','channel':'trades','chanId':41,'symbol':'tBTCUSD','pair':'BTCUSD'}");
                    server.sendJSON("[41,[[482083236,1595428778562,-0.00348216,9343.27028166],[482083234,1595428746373,0.00661892,9344.3],[482083233,1595428746373,0.00968224,9344.1],[482083231,1595428746373,0.00226506,9344.09137053],[482083230,1595428746373,0.13794872,9343.4],[482083229,1595428746373,0.01063592,9342.2],[482083228,1595428746373,0.04,9342.2],[482083227,1595428746373,0.04284914,9342.2],[482083226,1595428728960,0.00715086,9342.2],[482083225,1595428728960,0.01284914,9342.2],[482083223,1595428714671,-0.00711651,9342.1121303],[482083221,1595428711320,-0.00853146,9342.1121303],[482083220,1595428699510,0.0021,9342.2],[482083219,1595428698714,0.057986,9342.2],[482083217,1595428651782,-0.01,9342.1121303],[482083216,1595428651715,0.01718075,9342.2],[482083214,1595428651715,0.00281925,9342.18392002],[482083212,1595428639200,-0.01273494,9342.1121303],[482083210,1595428639200,-0.00226506,9342.18392002],[482083209,1595428629032,0.00535,9342.2],[482083208,1595428608678,0.19465273,9342.2],[482083207,1595428608678,0.0042,9342.2],[482083205,1595428608678,0.00114727,9342.18392002],[482083203,1595428606861,0.00111779,9342.18392002],[482083201,1595428544293,-0.29718075,9342.1121303],[482083199,1595428544293,-0.00281925,9342.18392002],[482083198,1595428510467,0.00500394,9342.2],[482083195,1595428458294,-0.22645823,9342.1121303],[482083193,1595428458294,-0.000615,9342.1121303],[482083191,1595428458294,-0.000615,9342.1121303]]]");
                    server.sendJSON("[33,'te',[482083238,1595428907203,-0.05,9343.3]]");
                    server.sendJSON("[41,'te',[482083239,1595428907203,-0.56969684,9343.3]]");
                    server.sendJSON("[41,'tu',[482083239,1595428907203,-0.56969684,9343.3]]");
                    server.sendJSON("[33,'tu',[482083238,1595428907203,-0.05,9343.3]]");
                });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 1;

        Execution exe = list.get(0);
        assert exe.id == 15954289072030000L;
        assert exe.direction == Direction.SELL;
        assert exe.price.is(9343.3);
        assert exe.size.is(0.56969684);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 14, 41, 47, 203));
        assert exe.consecutive == Execution.ConsecutiveDifference;
    }
}