/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.MarketService;
import cointoss.market.MarketProvider;

/**
 * @version 2018/07/26 22:52:51
 */
public final class BitFlyer extends MarketProvider {

    /** Market */
    public static final MarketService BTC_JPY = new BitFlyerService("BTC_JPY");

    /** Market */
    public static final MarketService FX_BTC_JPY = new BitFlyerService("FX_BTC_JPY");
}
