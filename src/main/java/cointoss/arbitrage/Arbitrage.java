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
import cointoss.market.MarketServiceProvider;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

public class Arbitrage {

    public Num size;

    public Market buyMarket;

    public Num buyPrice;

    public Market sellMarket;

    public Num sellPrice;

    public Num profit() {
        return sellPrice.minus(buyPrice).multiply(size).scale(sellMarket.service.setting.base.scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BUY: " + buyMarket + "@" + buyPrice + "  SELL:" + sellMarket + "@" + sellPrice + "  PROFIT: " + profit() + "(" + size + ")";
    }

    public static Signal<Arbitrage> by(Currency target, Currency base) {
        List<Market> markets = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(target, base))
                .map(Market::of)
                .toList();

        if (markets.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + target + "  Base: " + base + "]");
        }

        Num size = markets.stream().map(e -> e.service.setting.target.minimumSize).max(Comparator.naturalOrder()).get();

        Signal<Entry<Market, Num>> highestSellPrice = I.signal(markets)
                .combineLatestMap(market -> market.orderBook.longs.predictRealTakingPrice(I.signal(size)))
                .map(Arbitrage::max)
                .diff()
                .skip(e -> e.getValue().isZero());
        Signal<Entry<Market, Num>> lowestBuyPrice = I.signal(markets)
                .combineLatestMap(market -> market.orderBook.shorts.predictRealTakingPrice(I.signal(size)))
                .map(Arbitrage::min)
                .diff()
                .skip(e -> e.getValue().isZero());

        return highestSellPrice.combineLatest(lowestBuyPrice).map(e -> {
            Arbitrage arbitrage = new Arbitrage();
            arbitrage.buyMarket = e.ⅱ.getKey();
            arbitrage.buyPrice = e.ⅱ.getValue();
            arbitrage.sellMarket = e.ⅰ.getKey();
            arbitrage.sellPrice = e.ⅰ.getValue();
            arbitrage.size = Num.min(arbitrage.buyMarket.orderBook.shorts.bestSize(), arbitrage.sellMarket.orderBook.longs.bestSize());

            return arbitrage;
        });
    }

    static Entry<Market, Num> min(Map<Market, Num> markets) {
        return markets.entrySet().stream().min(Comparator.comparing(e -> e.getValue())).get();
    }

    static Entry<Market, Num> max(Map<Market, Num> markets) {
        return markets.entrySet().stream().max(Comparator.comparing(e -> e.getValue())).get();
    }

    public static void main(String[] args) throws InterruptedException {
        I.load(Market.class);

        Arbitrage.by(Currency.BTC, Currency.JPY).skip(a -> a.profit().isLessThan(100)).to(e -> {
            System.out.println(e);
        });

        Arbitrage.by(Currency.ETH, Currency.JPY).skip(a -> a.profit().isLessThan(100)).to(e -> {
            System.out.println(e);
        });

        Thread.sleep(1000 * 60 * 10);
    }
}
