/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.arbitrage;

import java.util.List;

import cointoss.Currency;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import kiss.Signal;

public class Arbitrage {

    public static Signal<Arbitrage> by(Currency target, Currency base) {
        List<MarketService> list = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(target, base))
                .toList();

        if (list.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + target + "  Base: " + base + "]");
        }

        double size = list.stream().mapToDouble(s -> s.setting.target.minimumSize.floatValue()).max().orElseThrow();

        return null;
    }
}
