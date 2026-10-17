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
     * A single bearish 1-minute kline.
     */
    private String kline() {
        return """
                [
                  [
                    1595393845045,
                    "9349.96",
                    "9350.00",
                    "9349.00",
                    "9349.50",
                    "1.00000000",
                    1595393905044,
                    "9349.73",
                    10,
                    "0.50000000",
                    "4674.86500000",
                    "0"
                  ]
                ]
                """;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executions() {
        httpClient.onGet().doReturn(kline());

        // Historical executions are generated from 1-minute klines.
        long id = (1595393845045L + 60_000) * 10_000;
        List<Execution> list = service.executionsBefore(id).waitForTerminate().toList();
        assert list.size() == 4;

        Execution e = list.get(0);
        assert e.id() == 1595393845045L * 10_000;
        assert e.orientation() == Direction.BUY;
        assert e.price().is(9349.96);
        assert e.size().is(0.25);
        assert e.date().isEqual(Chrono.utc(2020, 7, 22, 4, 57, 25, 45));
        assert e.consecutive() == Execution.ConsecutivePseudoDifference;

        e = list.get(1);
        assert e.id() == 1595393860045L * 10_000;
        assert e.orientation() == Direction.SELL;
        assert e.price().is(9350.00);
        assert e.size().is(0.25);
        assert e.date().isEqual(Chrono.utc(2020, 7, 22, 4, 57, 40, 45));
        assert e.consecutive() == Execution.ConsecutivePseudoDifference;

        e = list.get(2);
        assert e.id() == 1595393875045L * 10_000;
        assert e.orientation() == Direction.BUY;
        assert e.price().is(9349.00);
        assert e.size().is(0.25);
        assert e.date().isEqual(Chrono.utc(2020, 7, 22, 4, 57, 55, 45));
        assert e.consecutive() == Execution.ConsecutivePseudoDifference;

        e = list.get(3);
        assert e.id() == 1595393890045L * 10_000;
        assert e.orientation() == Direction.SELL;
        assert e.price().is(9349.50);
        assert e.size().is(0.25);
        assert e.date().isEqual(Chrono.utc(2020, 7, 22, 4, 58, 10, 45));
        assert e.consecutive() == Execution.ConsecutivePseudoDifference;
    }

    /**
     * Historical executions are generated from candles, so their IDs are based on the candle time
     * and rarely match the realtime trade IDs exactly. The handover from the historical data to the
     * realtime data must still be detected.
     */
    @Test
    public void checkEqualityOnHandoverToRealtime() {
        // a historical pseudo execution generated from a 1-minute candle (at the :00 boundary)
        Execution history = Execution.with.direction(Direction.BUY, 1)
                .id(1595401560000L * 10_000)
                .price(9331.91)
                .date(Chrono.utcByMills(1595401560000L));

        // a realtime trade which occurred 5.277 seconds later
        Execution realtime = Execution.with.direction(Direction.BUY, 1)
                .id(1595401565277L * 10_000)
                .price(9331.91)
                .date(Chrono.utcByMills(1595401565277L));

        // while the historical data is still behind the realtime data, keep emitting the historical
        // data instead of switching to the realtime stream
        assert !service.checkEquality(history, realtime);

        // the historical data (generated from the candle) has reached the realtime data
        assert service.checkEquality(realtime, realtime);

        Execution caughtUp = Execution.with.direction(Direction.SELL, 1)
                .id(1595401565277L * 10_000 + 1)
                .price(9331.91)
                .date(Chrono.utcByMills(1595401565278L));
        assert service.checkEquality(caughtUp, realtime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        httpClient.onGet().doReturn(kline());

        Execution e = service.executionLatest().waitForTerminate().to().exact();
        assert e.id() == 1595393890045L * 10_000;
        assert e.orientation() == Direction.SELL;
        assert e.price().is(9349.5);
        assert e.size().is(0.25);
        assert e.date().isEqual(Chrono.utc(2020, 7, 22, 4, 58, 10, 45));
        assert e.consecutive() == Execution.ConsecutivePseudoDifference;
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
        assert exe.id() == 1595401565277L * 10_000;
        assert exe.orientation() == Direction.BUY;
        assert exe.price().is(9331.91);
        assert exe.size().is(0.011601);
        assert exe.date().isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive() == Execution.ConsecutiveDifference;

        exe = list.get(1);
        assert exe.id() == 1595401565277L * 10_000 + 1;
        assert exe.orientation() == Direction.BUY;
        assert exe.price().is(9331.97);
        assert exe.size().is(0.001138);
        assert exe.date().isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive() == Execution.ConsecutiveSameBuyer;

        exe = list.get(2);
        assert exe.id() == 1595401565279L * 10_000;
        assert exe.orientation() == Direction.SELL;
        assert exe.price().is(9332.06);
        assert exe.size().is(0.006605);
        assert exe.date().isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 279));
        assert exe.consecutive() == Execution.ConsecutiveDifference;
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
        assert exe.id() == 1595401565277L * 10_000;
        assert exe.orientation() == Direction.BUY;
        assert exe.price().is(9331.97);
        assert exe.size().is(0.001138);
        assert exe.date().isEqual(Chrono.utc(2020, 7, 22, 7, 6, 5, 277));
        assert exe.consecutive() == Execution.ConsecutiveDifference;
    }
}
