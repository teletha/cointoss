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

import java.util.Comparator;
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

    public Num size;

    public static Signal<Arbitrage> by(Currency target, Currency base) {
        List<MarketService> services = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(target, base))
                .toList();

        if (services.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + target + "  Base: " + base + "]");
        }

        Num size = services.stream().map(e -> e.setting.target.minimumSize).max(Comparator.naturalOrder()).get();

        Signal<Entry<MarketService, Num>> highestSellPrice = I.signal(services)
                .combineLatestMap(service -> Market.of(service).orderBook.longs.predictTakingPrice(I.signal(size)))
                .map(Arbitrage::max)
                .skip(e -> e.getValue().isZero());
        Signal<Entry<MarketService, Num>> lowestBuyPrice = I.signal(services)
                .combineLatestMap(service -> Market.of(service).orderBook.shorts.predictTakingPrice(I.signal(size)))
                .map(Arbitrage::min)
                .skip(e -> e.getValue().isZero());

        return highestSellPrice.combineLatest(lowestBuyPrice).map(e -> {
            Arbitrage arbitrage = new Arbitrage();
            System.out.println(e.ⅰ + "      " + e.ⅱ);
            return arbitrage;
        });
    }

    static Entry<MarketService, Num> min(Map<MarketService, Num> markets) {
        return markets.entrySet().stream().min(Comparator.comparing(e -> e.getValue())).get();
    }

    static Entry<MarketService, Num> max(Map<MarketService, Num> markets) {
        return markets.entrySet().stream().max(Comparator.comparing(e -> e.getValue())).get();
    }

    public static void main(String[] args) throws InterruptedException {
        I.load(Market.class);

        Arbitrage.by(Currency.BTC, Currency.JPY).to(e -> {
        });

        Thread.sleep(1000 * 60 * 10);
    }
}
