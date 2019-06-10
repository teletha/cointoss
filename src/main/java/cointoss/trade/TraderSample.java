/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import cointoss.Market;
import cointoss.ticker.TickSpan;

public class TraderSample extends Trader2 {

    /**
     * @param market
     */
    public TraderSample(Market market) {
        super(market);

        market.tickers.tickerBy(TickSpan.Second30).each(tick -> {

        });
    }

}
