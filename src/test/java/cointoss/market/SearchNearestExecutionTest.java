/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.market.binance.Binance;
import cointoss.market.bitfinex.Bitfinex;
import cointoss.market.bitmex.BitMex;
import cointoss.market.bybit.Bybit;
import cointoss.market.coinbase.Coinbase;
import cointoss.market.ftx.FTX;
import cointoss.util.Chrono;

class SearchNearestExecutionTest {

    @Test
    void bitmex() {
        assert BitMex.XBT_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 160678079903700000L;
    }

    @Test
    void coinbase() {
        assert Coinbase.BTCUSD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 111789000;
    }

    @Test
    void binance() {
        assert Binance.BTC_USDT.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 443629856;
    }

    @Test
    void bybit() {
        assert Bybit.BTC_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 16067808009190000L;
    }

    @Test
    void ftx() {
        // need to normalize padding
        assert FTX.BTC_PERP.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id / 10 * 10 == 1606780799000L;
    }

    @Test
    void bitfinex() {
        assert Bitfinex.BTC_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 16067807986740000L;
    }
}
