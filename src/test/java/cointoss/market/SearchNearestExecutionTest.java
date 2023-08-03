/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import cointoss.market.binance.Binance;
import cointoss.market.bitfinex.Bitfinex;
import cointoss.market.bitmex.BitMex;
import cointoss.market.bybit.Bybit;
import cointoss.market.coinbase.Coinbase;
import cointoss.util.Chrono;

@Disabled
class SearchNearestExecutionTest {

    @Test
    @Timeout(value = 15)
    void bitmex() {
        assert BitMex.XBT_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 160678079903700000L;
    }

    @Test
    @Timeout(value = 15)
    void coinbase() {
        assert Coinbase.BTCUSD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 111789000;
    }

    @Test
    @Timeout(value = 15)
    void binance() {
        assert Binance.BTC_USDT.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 443629856;
    }

    @Test
    @Timeout(value = 15)
    void bybit() {
        assert Bybit.BTC_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 16067808009190000L;
    }

    @Test
    @Timeout(value = 15)
    void bitfinex() {
        assert Bitfinex.BTC_USD.searchNearestExecution(Chrono.utc(2020, 12, 1)).waitForTerminate().to().exact().id == 16067807986740000L;
    }
}