/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import cointoss.MarketService;
import cointoss.analyze.Analyzer;
import cointoss.analyze.ConsoleAnalyzer;
import cointoss.analyze.TradingStats;
import cointoss.execution.LogType;
import cointoss.trade.Trader;
import cointoss.util.Chrono;
import cointoss.util.Loggings;
import cointoss.util.arithmetic.Num;
import icy.manipulator.Icy;

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
    List<Trader> traders();

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
     * Use fast log.
     * 
     * @return
     */
    @Icy.Property
    default LogType type() {
        return LogType.Normal;
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

        Loggings.requestTradingLoggerReset();

        VerifiableMarket market = new VerifiableMarket(service());
        market.tickers.tickers().to(e -> e.ticks.disableMemorySaving());
        market.service.baseCurrency = initialBaseCurrency();
        market.service.targetCurrency = initialTargetCurrency();

        List<TradingStats> logs = new ArrayList();

        market.register(traders());
        analyzer.initialize(market, traders());

        LocalDateTime start = LocalDateTime.now();
        market.readLog(log -> log.range(start(), end(), type()).effect(market::perform).effectOnError(Throwable::printStackTrace));
        LocalDateTime end = LocalDateTime.now();

        for (Trader trader : traders()) {
            TradingStats log = trader.statistics();
            log.duration = Duration.between(start, end);
            logs.add(log);
        }
        analyzer.analyze(market, logs, detail());
    }
}