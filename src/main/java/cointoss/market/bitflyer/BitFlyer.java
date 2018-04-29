/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import cointoss.MarketLog;
import cointoss.MarketProvider;
import cointoss.MarketService;

/**
 * @version 2018/04/25 4:04:43
 */
public enum BitFlyer implements MarketProvider {
    BTC_JPY, FX_BTC_JPY, ETC_BTC, BCH_BTC;

    /** cache */
    private final MarketLog marketLog = new MarketLog(this);

    /** cache */
    private final MarketService marketBackend = new BitFlyerService(this, false);

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketLog log() {
        return marketLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketService service() {
        return marketBackend;
    }
}
