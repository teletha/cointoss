/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.binance;

import java.net.http.HttpClient;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;

public class BinancePublicServiceTest extends PublicServiceTestTemplate<BinanceService> {

    private class TestableService extends BinanceService {

        TestableService(String marketName, MarketSetting setting) {
            super(marketName, setting);
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
    protected BinanceService constructMarketService() {
        return new TestableService(Binance.BTC_USDT.marketName, Binance.BTC_USDT.setting);
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
                     "a": 2,
                     "p": "4261.48000000",
                     "q": "0.07518300",
                     "f": 2,
                     "l": 2,
                     "T": 1502942432322,
                     "m": false,
                     "M": true
                   },
                   {
                     "a": 3,
                     "p": "4280.56000000",
                     "q": "0.02960000",
                     "f": 3,
                     "l": 3,
                     "T": 1502942568879,
                     "m": false,
                     "M": true
                   }
                ]
                """);

        List<Execution> list = service.executionsAfter(1, 10).waitForTerminate().toList();
        Execution e = list.get(0);
        assert e.id == 2;
        assert e.orientation == Direction.BUY;
        assert e.price.is(4261.48);
        assert e.size.is(0.075183);
        assert e.date.isEqual(Chrono.utc(2017, 8, 17, 4, 00, 32, 322));
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id == 3;
        assert e.orientation == Direction.BUY;
        assert e.price.is(4280.56);
        assert e.size.is(0.0296);
        assert e.date.isEqual(Chrono.utc(2017, 8, 17, 4, 2, 48, 879));
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
                  {
                    "a": 330337340,
                    "p": "9349.96000000",
                    "q": "0.16990500",
                    "f": 359620557,
                    "l": 359620557,
                    "T": 1595393905045,
                    "m": true,
                    "M": true
                  }
                ]
                """);

        Execution e = service.executionLatest().waitForTerminate().to().exact();
        assert e.id == 330337340;
        assert e.orientation == Direction.SELL;
        assert e.price.is(9349.96);
        assert e.size.is(0.169905);
        assert e.date.isEqual(Chrono.utc(2020, 7, 22, 4, 58, 25, 45));
        assert e.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        websocketServer.replyWhenJSON("{'id':(\\d+),'method':'SUBSCRIBE','params':['btcusdt@aggTrade']}", server -> {
            server.sendJSON("{'result':null,'id':$1}");
            server.sendJSON("{'stream':'btcusdt@aggTrade','data':{'e':'aggTrade','E':1595401565277,'s':'BTCUSDT','a':330377764,'p':'9331.91000000','q':'0.01160100','f':359665720,'l':359665720,'T':1595401565277,'m':false,'M':true}}");
            server.sendJSON("{'stream':'btcusdt@aggTrade','data':{'e':'aggTrade','E':1595401565277,'s':'BTCUSDT','a':330377765,'p':'9331.97000000','q':'0.00113800','f':359665721,'l':359665721,'T':1595401565277,'m':false,'M':true}}");
            server.sendJSON("{'stream':'btcusdt@aggTrade','data':{'e':'aggTrade','E':1595401565279,'s':'BTCUSDT','a':330377766,'p':'9332.06000000','q':'0.00660500','f':359665722,'l':359665722,'T':1595401565279,'m':true,'M':true}}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 3;

        Execution exe = list.get(0);
        assert exe.id == 330377764;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9331.91);
        assert exe.size.is(0.011601);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id == 330377765;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9331.97);
        assert exe.size.is(0.001138);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive == Execution.ConsecutiveDifference;

        exe = list.get(2);
        assert exe.id == 330377766;
        assert exe.orientation == Direction.SELL;
        assert exe.price.is(9332.06);
        assert exe.size.is(0.006605);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 279));
        assert exe.consecutive == Execution.ConsecutiveDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executionRealtimelyConsecutiveBuy() {
        // binance has no consecutive data
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executionRealtimelyConsecutiveSell() {
        // binance has no consecutive data
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        websocketServer.replyWhenJSON("{'id':(\\d+),'method':'SUBSCRIBE','params':['btcusdt@aggTrade']}", server -> {
            server.sendJSON("{'result':null,'id':$1}");
            server.sendJSON("{'stream':'ignored@aggTrade','data':{'e':'aggTrade','E':1595401565277,'s':'BTCUSDT','a':330377764,'p':'9331.91000000','q':'0.01160100','f':359665720,'l':359665720,'T':1595401565277,'m':false,'M':true}}");
            server.sendJSON("{'stream':'btcusdt@aggTrade','data':{'e':'aggTrade','E':1595401565277,'s':'BTCUSDT','a':330377765,'p':'9331.97000000','q':'0.00113800','f':359665721,'l':359665721,'T':1595401565277,'m':false,'M':true}}");
        });

        List<Execution> list = service.executionsRealtimely().toList();
        assert list.size() == 1;

        Execution exe = list.get(0);
        assert exe.id == 330377765;
        assert exe.orientation == Direction.BUY;
        assert exe.price.is(9331.97);
        assert exe.size.is(0.001138);
        assert exe.date.isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive == Execution.ConsecutiveDifference;
    }
}