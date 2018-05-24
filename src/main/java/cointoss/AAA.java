/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;

import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;

/**
 * @version 2018/05/24 9:08:50
 */
public class AAA {
    /**
     * @param args
     */
    public static void main(String[] args) {
        ZonedDateTime start = ZonedDateTime.of(2018, 5, 3, 0, 0, 0, 0, Chrono.UTC);
        ZonedDateTime end = start.plusDays(20);

        MarketLog log = new MarketLog(BitFlyerService.FX_BTC_JPY);
        log.range(start, end).to(e -> {

        });
    }
}
