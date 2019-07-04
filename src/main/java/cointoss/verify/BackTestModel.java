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

import com.google.common.base.Stopwatch;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Chrono;
import cointoss.util.Num;
import icy.manipulator.Icy;
import kiss.I;

@Icy
public abstract class BackTestModel {

    public final Stopwatch time = Stopwatch.createUnstarted();

    @Icy.Property
    public abstract MarketService service();

    @Icy.Property
    public abstract ZonedDateTime start();

    @Icy.Overload("start")
    private ZonedDateTime start(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    @Icy.Property
    public abstract ZonedDateTime end();

    @Icy.Overload("end")
    private ZonedDateTime end(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    @Icy.Property
    public Num initialBaseCurrency() {
        return Num.ZERO;
    }

    @Icy.Overload("initialBaseCurrency")
    private Num initialBaseCurrency(double value) {
        return Num.of(value);
    }

    @Icy.Property
    public Num initialTargetCurrency() {
        return Num.ZERO;
    }

    @Icy.Overload("initialTargetCurrency")
    private Num initialTargetCurrency(double value) {
        return Num.of(value);
    }

    public final List<TradingLog> run(Function<Market, Trader> traderBuilder) {
        return runs(market -> List.of(traderBuilder.apply(market)));
    }

    public final List<TradingLog> runs(Function<Market, List<Trader>> traderBuilder) {
        VerifiableMarket market = new VerifiableMarket(service());
        market.service.baseCurrency = initialBaseCurrency();
        market.service.targetCurrency = initialTargetCurrency();

        List<Trader> traders = traderBuilder.apply(market);

        time.start();
        market.readLog(log -> log.range(start(), end()).effect(e -> market.perform(e)).effectOnError(e -> {
            e.printStackTrace();
        }).effectOnComplete(() -> {
            System.out.println("complete");
        }));
        time.stop();

        return I.signal(traders).map(Trader::log).toList();
    }
}
