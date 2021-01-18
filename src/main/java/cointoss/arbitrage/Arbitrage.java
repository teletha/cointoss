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
import java.util.Map;
import java.util.Map.Entry;

import cointoss.Currency;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class Arbitrage {

    public static Signal<Arbitrage> by(Currency target, Currency base) {
        List<MarketService> services = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(target, base))
                .toList();

        if (services.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + target + "  Base: " + base + "]");
        }

        Signal<Num> size = I.signal(services).map(service -> service.setting.target.minimumSize).scanWith(Num.ZERO, Num::max).last();
        Signal<Entry<MarketService, Num>> maxSellPrice = I.signal(services)
                .combineLatestMap(service -> Market.of(service).orderBook.longs.predictTakingPrice(size))
                .map(Arbitrage::max);
        Signal<Entry<MarketService, Num>> minBuyPrice = I.signal(services)
                .combineLatestMap(service -> Market.of(service).orderBook.shorts.predictTakingPrice(size))
                .map(Arbitrage::min);

        return maxSellPrice.combineLatest(minBuyPrice).map(e -> {
            Arbitrage arbitrage = new Arbitrage();
            return arbitrage;
        });
    }

    static Entry<MarketService, Num> min(Map<MarketService, Num> markets) {
        return null;
    }

    static Entry<MarketService, Num> max(Map<MarketService, Num> markets) {
        return null;
    }
}
