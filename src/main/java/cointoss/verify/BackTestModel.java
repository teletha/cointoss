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
import cointoss.analyze.Analyzer;
import cointoss.analyze.ConsoleAnalyzer;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Chrono;
import cointoss.util.Num;
import icy.manipulator.Icy;

@Icy
public interface BackTestModel {

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
     * Set the emulatiom mode.
     * 
     * @return
     */
    @Icy.Property
    default boolean exclusiveExecution() {
        return true;
    }

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
     * Run with {@link Trader}s.
     * 
     * @param traderBuilder
     * @return
     */
    default void run(Function<Market, Trader> traderBuilder) {
        run(traderBuilder, new ConsoleAnalyzer());
    }

    /**
     * Run with {@link Trader}s.
     * 
     * @param traderBuilder
     * @return
     */
    default void run(Function<Market, Trader> traderBuilder, Analyzer visualizer) {
        runs(market -> List.of(traderBuilder.apply(market)), visualizer);
    }

    /**
     * Run with {@link Trader}s.
     * 
     * @param traderBuilder
     * @return
     */
    default void runs(Function<Market, List<Trader>> traderBuilder) {
        runs(traderBuilder, new ConsoleAnalyzer());
    }

    /**
     * Run with {@link Trader}s.
     * 
     * @param traderBuilder
     * @return
     */
    default void runs(Function<Market, List<Trader>> traderBuilder, Analyzer visualizer) {
        VerifiableMarket market = new VerifiableMarket(service());
        market.service.exclusiveExecution = exclusiveExecution();
        market.service.baseCurrency = initialBaseCurrency();
        market.service.targetCurrency = initialTargetCurrency();

        List<TradingLog> logs = new ArrayList();
        List<Trader> traders = traderBuilder.apply(market);

        LocalDateTime start = LocalDateTime.now();
        market.readLog(log -> log.range(start(), end()).effect(e -> market.perform(e)));
        LocalDateTime end = LocalDateTime.now();

        for (Trader trader : traders) {
            TradingLog log = trader.log();
            log.duration = Duration.between(start, end);
            logs.add(log);
        }

        if (visualizer != null) {
            visualizer.analyze(logs);
        }
    }
}
