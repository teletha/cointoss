/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitmex;

import java.net.http.HttpClient;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;

class BitmexPublicServiceTest extends PublicServiceTestTemplate<BitMexService> {

    private class TestableService extends BitMexService {

        TestableService(MarketService service) {
            super(88, service.marketName, service.setting);
        }

        @Override
        protected HttpClient client() {
            return httpClient;
        }

        @Override
        protected EfficientWebSocket clientRealtimely() {
            try {
                if (websocketServer.hasReplyRule()) {
                    return super.clientRealtimely().withClient(websocketServer.httpClient()).withScheduler(chronus);
                } else {
                    usedRealWebSocket = true;
                    return super.clientRealtimely().enableDebug();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BitMexService constructMarketService() {
        return new TestableService(BitMex.XBT_USD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executions() {
        httpClient.onGet().doReturn("""
                [
                  {
                    "timestamp": "2015-09-30T08:57:15.828Z",
                    "symbol": "XBTUSD",
                    "side": "Sell",
                    "size": 10,
                    "price": 235.74,
                    "tickDirection": "MinusTick",
                    "trdMatchID": "85b088b4-80c5-95ca-7be8-f34cae7ef2b1",
                    "grossValue": 2357400,
                    "homeNotional": 0.023574,
                    "foreignNotional": 5.55733476
                  },
                  {
                    "timestamp": "2015-09-30T08:57:36.358Z",
                    "symbol": "XBTUSD",
                    "side": "Buy",
                    "size": 9,
                    "price": 235.9,
                    "tickDirection": "PlusTick",
                    "trdMatchID": "f157ae33-5c9d-1a28-e76c-d12f685d5843",
                    "grossValue": 2123100,
                    "homeNotional": 0.021231,
                    "foreignNotional": 5.0083929
                  }
                ]
                """);

        List<Execution> list = service.executions(1, 10).waitForTerminate().toList();
        Execution e = list.get(0);
        assert e.id() == 144360343582800000L;
        assert e.orientation == Direction.SELL;
        assert e.price.is(235.74);
        assert e.size.is(0.023574);
        assert e.date.isEqual(Chrono.utc(2015, 9, 30, 8, 57, 15, 828));
        assert e.consecutive() == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id() == 144360345635800000L;
        assert e.orientation == Direction.BUY;
        assert e.price.is(235.9);
        assert e.size.is(0.021231);
        assert e.date.isEqual(Chrono.utc(2015, 9, 30, 8, 57, 36, 358));
        assert e.consecutive() == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        httpClient.onGet().doReturn("""
                [
                  {
                    "timestamp": "2020-07-22T16:34:27.324Z",
                    "symbol": "XBTUSD",
                    "side": "Buy",
                    "size": 1,
                    "price": 9344.5,
                    "tickDirection": "ZeroPlusTick",
                    "trdMatchID": "06cde094-96ba-9878-2f8b-534aef8b1270",
                    "grossValue": 10701,
                    "homeNotional": 0.00010701,
                    "foreignNotional": 1
                  }
                ]
                """);

        Execution e = service.executionLatest().waitForTerminate().to().exact();
        assert e.id() == 159543566732400000L;
        assert e.orientation == Direction.BUY;
        assert e.price.is(9344.5);
        assert e.size.is(0.00010701);
        assert e.date.isEqual(Chrono.utc(2020, 7, 22, 16, 34, 27, 324));
        assert e.consecutive() == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        websocketServer.replyWhenJSON("{'args':['trade:XBTUSD'],'op':'subscribe'}", server -> {
            server.sendJSON("{'info':'Welcome to the BitMEX Realtime API.','version':'2020-07-14T21:30:54.000Z','timestamp':'2020-07-22T16:36:44.022Z','docs':'https://www.bitmex.com/app/wsAPI','limit':{'remaining':39}}");
            server.sendJSON("{'success':true,'subscribe':'trade:XBTUSD','request':{'args':['trade:XBTUSD'],'op':'subscribe'}}");
            server.sendJSON("{'table':'trade','action':'partial','keys':[],'types':{'timestamp':'timestamp','symbol':'symbol','side':'symbol','size':'long','price':'float','tickDirection':'symbol','trdMatchID':'guid','grossValue':'long','homeNotional':'float','foreignNotional':'float'},'foreignKeys':{'symbol':'instrument','side':'side'},'attributes':{'timestamp':'sorted','symbol':'grouped'},'filter':{'symbol':'XBTUSD'},'data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Buy','size':102,'price':9344.5,'tickDirection':'PlusTick','trdMatchID':'e482b0d5-5749-9cc5-23d9-864514816f53','grossValue':1091502,'homeNotional':0.01091502,'foreignNotional':102}]}");
            server.sendJSON("{'table':'trade','action':'insert','data':[{'timestamp':'2020-07-22T16:36:48.264Z','symbol':'XBTUSD','side':'Buy','size':5000,'price':9344.5,'tickDirection':'ZeroPlusTick','trdMatchID':'0890e57d-366f-0394-6fac-3fac3b5b5569','grossValue':53505000,'homeNotional':0.53505,'foreignNotional':5000}]}");
            server.sendJSON("{'table':'trade','action':'insert','data':[{'timestamp':'2020-07-22T16:36:53.792Z','symbol':'XBTUSD','side':'Sell','size':1,'price':9344,'tickDirection':'MinusTick','trdMatchID':'f92979ae-b84a-8e0e-07b5-aa3ab590f0b7','grossValue':10702,'homeNotional':0.00010702,'foreignNotional':1}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 3;

        Execution exe = list.get(0);
        assert exe.id() == 159543580344000000L;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.01091502);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id() == 159543580826400000L;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.53505);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 48, 264));
        assert exe.consecutive() == Execution.ConsecutiveDifference;

        exe = list.get(2);
        assert exe.id() == 159543581379200000L;
        assert exe.orientation == Direction.SELL;
        assert exe.price.is(9344);
        assert exe.size.is(0.00010702);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 53, 792));
        assert exe.consecutive() == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        websocketServer.replyWhenJSON("{'args':['trade:XBTUSD'],'op':'subscribe'}", server -> {
            server.sendJSON("{'info':'Welcome to the BitMEX Realtime API.','version':'2020-07-14T21:30:54.000Z','timestamp':'2020-07-22T16:36:44.022Z','docs':'https://www.bitmex.com/app/wsAPI','limit':{'remaining':39}}");
            server.sendJSON("{'success':true,'subscribe':'trade:XBTUSD','request':{'args':['trade:XBTUSD'],'op':'subscribe'}}");
            server.sendJSON("{'table':'trade','action':'partial','keys':[],'types':{'timestamp':'timestamp','symbol':'symbol','side':'symbol','size':'long','price':'float','tickDirection':'symbol','trdMatchID':'guid','grossValue':'long','homeNotional':'float','foreignNotional':'float'},'foreignKeys':{'symbol':'instrument','side':'side'},'attributes':{'timestamp':'sorted','symbol':'grouped'},'filter':{'symbol':'XBTUSD'},'data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Buy','size':102,'price':9344.5,'tickDirection':'PlusTick','trdMatchID':'e482b0d5-5749-9cc5-23d9-864514816f53','grossValue':1091502,'homeNotional':0.01091502,'foreignNotional':102}]}");
            server.sendJSON("{'table':'trade','action':'insert','data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Buy','size':5000,'price':9344.5,'tickDirection':'ZeroPlusTick','trdMatchID':'0890e57d-366f-0394-6fac-3fac3b5b5569','grossValue':53505000,'homeNotional':0.53505,'foreignNotional':5000}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;

        Execution exe = list.get(0);
        assert exe.id() == 159543580344000000L;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.01091502);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id() == 159543580344000001L;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.53505);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveSameBuyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        websocketServer.replyWhenJSON("{'args':['trade:XBTUSD'],'op':'subscribe'}", server -> {
            server.sendJSON("{'info':'Welcome to the BitMEX Realtime API.','version':'2020-07-14T21:30:54.000Z','timestamp':'2020-07-22T16:36:44.022Z','docs':'https://www.bitmex.com/app/wsAPI','limit':{'remaining':39}}");
            server.sendJSON("{'success':true,'subscribe':'trade:XBTUSD','request':{'args':['trade:XBTUSD'],'op':'subscribe'}}");
            server.sendJSON("{'table':'trade','action':'partial','keys':[],'types':{'timestamp':'timestamp','symbol':'symbol','side':'symbol','size':'long','price':'float','tickDirection':'symbol','trdMatchID':'guid','grossValue':'long','homeNotional':'float','foreignNotional':'float'},'foreignKeys':{'symbol':'instrument','side':'side'},'attributes':{'timestamp':'sorted','symbol':'grouped'},'filter':{'symbol':'XBTUSD'},'data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Sell','size':102,'price':9344.5,'tickDirection':'PlusTick','trdMatchID':'e482b0d5-5749-9cc5-23d9-864514816f53','grossValue':1091502,'homeNotional':0.01091502,'foreignNotional':102}]}");
            server.sendJSON("{'table':'trade','action':'insert','data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Sell','size':5000,'price':9344.5,'tickDirection':'ZeroPlusTick','trdMatchID':'0890e57d-366f-0394-6fac-3fac3b5b5569','grossValue':53505000,'homeNotional':0.53505,'foreignNotional':5000}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 2;

        Execution exe = list.get(0);
        assert exe.id() == 159543580344000000L;
        assert exe.orientation == Direction.SELL;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.01091502);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id() == 159543580344000001L;
        assert exe.orientation == Direction.SELL;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.53505);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveSameSeller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        websocketServer.replyWhenJSON("{'args':['trade:XBTUSD'],'op':'subscribe'}", server -> {
            server.sendJSON("{'info':'Welcome to the BitMEX Realtime API.','version':'2020-07-14T21:30:54.000Z','timestamp':'2020-07-22T16:36:44.022Z','docs':'https://www.bitmex.com/app/wsAPI','limit':{'remaining':39}}");
            server.sendJSON("{'success':true,'subscribe':'trade:XBTUSD','request':{'args':['trade:XBTUSD'],'op':'subscribe'}}");
            server.sendJSON("{'table':'trade','action':'partial','keys':[],'types':{'timestamp':'timestamp','symbol':'symbol','side':'symbol','size':'long','price':'float','tickDirection':'symbol','trdMatchID':'guid','grossValue':'long','homeNotional':'float','foreignNotional':'float'},'foreignKeys':{'symbol':'instrument','side':'side'},'attributes':{'timestamp':'sorted','symbol':'grouped'},'filter':{'symbol':'XBTUSD'},'data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'XBTUSD','side':'Sell','size':102,'price':9344.5,'tickDirection':'PlusTick','trdMatchID':'e482b0d5-5749-9cc5-23d9-864514816f53','grossValue':1091502,'homeNotional':0.01091502,'foreignNotional':102}]}");
            server.sendJSON("{'table':'trade','action':'insert','data':[{'timestamp':'2020-07-22T16:36:43.440Z','symbol':'ETHUSD','side':'Sell','size':5000,'price':9344.5,'tickDirection':'ZeroPlusTick','trdMatchID':'0890e57d-366f-0394-6fac-3fac3b5b5569','grossValue':53505000,'homeNotional':0.53505,'foreignNotional':5000}]}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 1;

        Execution exe = list.get(0);
        assert exe.id() == 159543580344000000L;
        assert exe.orientation == Direction.SELL;
        assert exe.price.is(9344.5);
        assert exe.size.is(0.01091502);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 16, 36, 43, 440));
        assert exe.consecutive() == Execution.ConsecutiveDifference;
    }
}