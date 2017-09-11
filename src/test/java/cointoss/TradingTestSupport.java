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

import org.junit.Before;

import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/11 13:34:54
 */
public abstract class TradingTestSupport extends Trading {

    protected TestableMarket market;

    /**
     * @param market
     */
    public TradingTestSupport() {
        super(new TestableMarket());

        this.market = (TestableMarket) super.market;
        this.market.tradings.add(this);
    }

    @Before
    public void init() {
        position = null;
        positionPrice = Decimal.ZERO;
        positionSize = Decimal.ZERO;

        requestEntrySize = Decimal.ZERO;
        requestExitSize = Decimal.ZERO;

        close();
    }
}
