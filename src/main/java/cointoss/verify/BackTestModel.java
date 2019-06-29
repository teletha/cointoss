/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.verify;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Chrono;
import icy.manipulator.Icy;
import kiss.I;

@Icy
public interface BackTestModel {

    @Icy.Property
    MarketService service();

    @Icy.Property
    ZonedDateTime start();

    @Icy.Overload("start")
    private ZonedDateTime start(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    @Icy.Property
    ZonedDateTime end();

    @Icy.Overload("end")
    private ZonedDateTime end(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    default List<TradingLog> run(Function<Market, Trader> traderBuilder) {
        return runs(market -> List.of(traderBuilder.apply(market)));
    }

    default List<TradingLog> runs(Function<Market, List<Trader>> traderBuilder) {
        VerifiableMarket market = new VerifiableMarket(service());

        List<Trader> traders = traderBuilder.apply(market);

        market.readLog(log -> log.range(start(), end()).effect(e -> market.perform(e)).effectOnError(e -> {
            e.printStackTrace();
        }).effectOnComplete(() -> {
            System.out.println("complete");
        }));

        return I.signal(traders).map(Trader::log).toList();
    }
}
