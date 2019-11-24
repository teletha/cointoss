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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.Trader;
import cointoss.analyze.Analyzer;
import cointoss.analyze.ConsoleAnalyzer;
import cointoss.analyze.TradingStatistics;
import cointoss.execution.Execution;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import cointoss.util.Chrono;
import cointoss.util.Num;
import icy.manipulator.Icy;
import kiss.I;

@Icy
interface BackTestModel {

    /**
     * Set the target market.
     * 
     * @return
     */
    @Icy.Property
    MarketService service();

    /**
     * Set the start date.
     * 
     * @return
     */
    @Icy.Property
    ZonedDateTime start();

    /**
     * Set the start date.
     * 
     * @return
     */
    @Icy.Overload("start")
    private ZonedDateTime start(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    /**
     * Set the end date.
     * 
     * @return
     */
    @Icy.Property
    ZonedDateTime end();

    /**
     * Set the end date.
     * 
     * @return
     */
    @Icy.Overload("end")
    private ZonedDateTime end(int year, int month, int day) {
        return Chrono.utc(LocalDate.of(year, month, day));
    }

    /**
     * Set the end date.
     * 
     * @return
     */
    @Icy.Property
    List<Function<Market, cointoss.Trader>> traders();

    /**
     * Set the initial assets.
     * 
     * @return
     */
    @Icy.Property
    default Num initialBaseCurrency() {
        return Num.ZERO;
    }

    /**
     * Set the initial assets.
     * 
     * @return
     */
    @Icy.Overload("initialBaseCurrency")
    private Num initialBaseCurrency(double value) {
        return Num.of(value);
    }

    /**
     * Set the initial assets.
     * 
     * @return
     */
    @Icy.Property
    default Num initialTargetCurrency() {
        return Num.ZERO;
    }

    /**
     * Set the initial assets.
     * 
     * @return
     */
    @Icy.Overload("initialTargetCurrency")
    private Num initialTargetCurrency(double value) {
        return Num.of(value);
    }

    /**
     * Set the detail option.
     * 
     * @return
     */
    @Icy.Property
    default boolean detail() {
        return false;
    }

    /**
     * Activate test with {@link ConsoleAnalyzer}.
     */
    default void run() {
        run(null);
    }

    /**
     * Activate test with your {@link Analyzer}.
     * 
     * @param analyzer
     */
    default void run(Analyzer analyzer) {
        if (analyzer == null) {
            analyzer = new ConsoleAnalyzer();
        }

        VerifiableMarket market = new VerifiableMarket(service());
        market.service.baseCurrency = initialBaseCurrency();
        market.service.targetCurrency = initialTargetCurrency();

        analyzer.initialize(market);

        List<TradingStatistics> logs = new ArrayList();
        List<Trader> traders = I.signal(traders()).map(t -> t.apply(market)).toList();

        LocalDateTime start = LocalDateTime.now();
        market.readLog(log -> log.range(start(), end()).effect(market::perform).effectOnComplete(() -> {
            // Since a display that matches the actual final result can be expected, a dummy tick is
            // added at the end.
            Tick last = market.tickers.of(Span.Second5).last();
            market.perform(Execution.with.buy(market.service.setting.targetCurrencyMinimumBidSize).price(last.closePrice()).date(last.end));
        }).effectOnError(Throwable::printStackTrace));
        LocalDateTime end = LocalDateTime.now();

        for (Trader trader : traders) {
            TradingStatistics log = trader.statistics();
            log.duration = Duration.between(start, end);
            logs.add(log);
        }
        analyzer.analyze(market, logs, detail());
    }
}
