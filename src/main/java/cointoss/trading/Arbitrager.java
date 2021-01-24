/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trading;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cointoss.Currency;
import cointoss.Direction;
import cointoss.Market;
import cointoss.arbitrage.Arbitrage;
import cointoss.market.MarketServiceProvider;
import cointoss.trade.Funds;
import cointoss.trade.Trader;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Managed;
import kiss.Signal;

public class Arbitrager extends Trader {

    /** The configurable target currency. */
    @Managed
    public Currency targetCurrency;

    /** The configurable base currency. */
    @Managed
    public Currency baseCurrency;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declareStrategy(Market market, Funds fund) {
        List<Market> markets = MarketServiceProvider.availableMarketServices()
                .take(service -> service.setting.type.isSpot())
                .take(service -> service.setting.match(targetCurrency, baseCurrency))
                .map(Market::trainingOf)
                .toList();

        if (markets.size() <= 1) {
            throw new Error("There must be at least two exchanges that are eligible. [Target: " + targetCurrency + "  Base: " + baseCurrency + "]");
        }

        Num size = markets.stream().map(e -> e.service.setting.target.minimumSize).max(Comparator.naturalOrder()).get();
        Signal<Entry<Market, Num>> highestSellPrice = I.signal(markets)
                .combineLatestMap(m -> m.orderBook.longs.predictRealTakingPrice(I.signal(size)))
                .map(this::max)
                .diff()
                .skip(e -> e.getValue().isZero());
        Signal<Entry<Market, Num>> lowestBuyPrice = I.signal(markets)
                .combineLatestMap(m -> m.orderBook.shorts.predictRealTakingPrice(I.signal(size)))
                .map(this::min)
                .diff()
                .skip(e -> e.getValue().isZero());

        highestSellPrice.combineLatest(lowestBuyPrice).map(e -> {

            return null;
        });

        when(Arbitrage.by(Currency.ETH, Currency.JPY), arb -> {

            trade(arb.buyMarket, e -> {
                e.entry(Direction.BUY, arb.size);
            }, e -> {
                e.exitWhen(arb.closeBuy(e.entryPrice));
            });

            trade(arb.sellMarket, e -> {
                e.entry(Direction.SELL, arb.size);
            }, e -> {
                e.exitWhen(arb.closeSell(e.entryPrice));
            });
        });
    }

    Entry<Market, Num> min(Map<Market, Num> markets) {
        return markets.entrySet().stream().min(Comparator.comparing(e -> e.getValue())).get();
    }

    Entry<Market, Num> max(Map<Market, Num> markets) {
        return markets.entrySet().stream().max(Comparator.comparing(e -> e.getValue())).get();
    }
}
