/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import org.junit.Test;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/11 13:24:46
 */
public class TradingStrategyTest extends TradingStrategyTestSupport {

    @Test
    public void entryLimit() throws Exception {
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // try entry
        entryLimit(Side.BUY, Decimal.ONE, Decimal.TEN, null);
        assert requestEntrySize.is(1);
        assert positionSize.is(0);

        // execute
        market.execute(1, 10);
        assert requestEntrySize.is(0);
        assert positionSize.is(1);
    }

    @Test
    public void entryLimitInvalidParameters() throws Exception {
        // null side
        entryLimit(null, Decimal.ONE, Decimal.ONE, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // null size
        entryLimit(Side.BUY, null, Decimal.ONE, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // zero size
        entryLimit(Side.BUY, Decimal.ZERO, Decimal.ONE, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // negative size
        entryLimit(Side.BUY, Decimal.valueOf(-1), Decimal.ONE, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // null price
        entryLimit(Side.BUY, Decimal.ONE, null, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // zero price
        entryLimit(Side.BUY, Decimal.ONE, Decimal.ZERO, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // negative price
        entryLimit(Side.BUY, Decimal.ONE, Decimal.valueOf(-1), null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);
    }

    @Test
    public void entryMarket() throws Exception {
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // try entry
        entryMarket(Side.BUY, Decimal.ONE, null);
        assert requestEntrySize.is(1);
        assert positionSize.is(0);

        // execute
        market.execute(1, 10);
        assert requestEntrySize.is(0);
        assert positionSize.is(1);
    }

    @Test
    public void entryMarketInvalidParameters() throws Exception {
        // null side
        entryMarket(null, Decimal.ONE, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // null size
        entryMarket(Side.BUY, null, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // zero size
        entryMarket(Side.BUY, Decimal.ZERO, null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);

        // negative size
        entryMarket(Side.BUY, Decimal.valueOf(-1), null);
        assert requestEntrySize.is(0);
        assert positionSize.is(0);
    }

    @Test
    public void exitMarket() throws Exception {
        // entry and execute
        entryLimit(Side.BUY, Decimal.ONE, Decimal.TEN, null);
        market.execute(1, 10);
        assert requestEntrySize.is(0);
        assert positionSize.is(1);

        // try exit
        exitMarket(Decimal.ONE);
        assert requestEntrySize.is(0);
        assert requestExitSize.is(1);
        assert positionSize.is(1);

        market.execute(1, 10);
        assert requestEntrySize.is(0);
        assert requestExitSize.is(0);
        assert positionSize.is(0);
    }
}
