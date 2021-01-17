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
import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class Arbitrage {

    /** The target. */
    private final List<Market> markets;

    private final Num min;

    private Arbitrage(List<MarketService> services) {
        this.markets = I.signal(services).map(Market::of).toList();
        this.min = Num.ONE;

        I.signal(markets).flatMap(m -> m.orderBook.longs.predictTakingPrice(I.signal(min))).to(e -> {

        });
    }

    public static Signal<Arbitrage> by(Currency target, Currency base) {
        List<MarketService> list = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(target, base))
                .toList();

        if (list.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + target + "  Base: " + base + "]");
        }

        I.signal(list).combineLatestMap2(s -> Market.of(s).orderBook.longs.predictTakingPrice(I.signal(Num.ONE)));
        Arbitrage arbitrage = new Arbitrage(list);

        return null;
    }
}
