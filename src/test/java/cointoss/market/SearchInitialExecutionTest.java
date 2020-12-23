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

class SearchInitialExecutionTest {

    @Test
    void bitmex() {
        assert BitMex.XBT_USD.searchInitialExecution().waitForTerminate().to().exact().id == 144318446570600000L;
    }

    @Test
    void coinbase() {
        assert Coinbase.BTCUSD.searchInitialExecution().waitForTerminate().to().exact().id == 1;
    }

    @Test
    void binance() {
        assert Binance.BTC_USDT.searchInitialExecution().waitForTerminate().to().exact().id == 0;
    }

    @Test
    void bybit() {
        assert Bybit.BTC_USD.searchInitialExecution().waitForTerminate().to().exact().id == 15698880007190000L;
    }

    @Test
    void ftx() {
        assert FTX.BTC_PERP.searchInitialExecution().waitForTerminate().to().exact().id == 1551788809000L;
    }

    @Test
    void bitfinex() {
        assert Bitfinex.BTC_USD.searchInitialExecution().waitForTerminate().to().exact().id == 13581820430000000L;
    }
}