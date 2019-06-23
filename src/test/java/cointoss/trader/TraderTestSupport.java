/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trader;

import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class TraderTestSupport extends Trader {

    protected VerifiableMarket market;

    protected TradingLog log;

    /**
     * @param provider
     */
    public TraderTestSupport() {
        super(new VerifiableMarket());

        this.market = (VerifiableMarket) super.market;
    }

    /**
     * Timing function.
     * 
     * @return
     */
    protected final Signal<?> now() {
        return I.signal("ok");
    }

    /**
     * Create current log.
     * 
     * @return
     */
    protected final TradingLog createLog() {
        return new TradingLog(market, I.list(this));
    }
}
