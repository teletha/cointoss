/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import java.time.LocalTime;
import java.util.Map;
import java.util.TreeMap;

import cointoss.BackTester;
import cointoss.Trading;
import cointoss.market.bitflyer.BitFlyer;
import eu.verdelhan.ta4j.Decimal;

/**
 * @version 2017/09/20 2:36:26
 */
public class FXPatternAnalyzer extends Trading {

    private Map<LocalTime, Statistics> statistics = new TreeMap();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.hour1.signal().to(tick -> {
            Statistics stat = statistics.computeIfAbsent(tick.start.toLocalTime().withMinute(0).withSecond(0).withNano(0), Statistics::new);
            Decimal diff = tick.closePrice.dividedBy(tick.openPrice);

            if (tick.openPrice.isLessThan(tick.closePrice)) {
                stat.up = stat.up.plus(1);
                stat.upRatio = stat.upRatio.multipliedBy(diff);
            } else {
                stat.down = stat.down.plus(1);
                stat.downRatio = stat.downRatio.multipliedBy(diff);
            }
        });
    }

    /**
     * Analyze
     * 
     * @param args
     */
    public static void main(String[] args) {
        FXPatternAnalyzer analyzer = new FXPatternAnalyzer();

        BackTester.with()
                .baseCurrency(1000000)
                .targetCurrency(0)
                .log(BitFlyer.FX_BTC_JPY.log().rangeAll())
                .strategy(analyzer)
                .trial(1)
                .run();

        for (Statistics value : analyzer.statistics.values()) {
            System.out.println(value);
        }
    }

    /**
     * @version 2017/09/20 15:36:57
     */
    private static class Statistics {

        public Decimal down = Decimal.ZERO;

        public Decimal downRatio = Decimal.ONE;

        public Decimal up = Decimal.ZERO;

        public Decimal upRatio = Decimal.ONE;

        private final LocalTime time;

        /**
         * @param time
         */
        public Statistics(LocalTime time) {
            this.time = time;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new StringBuilder().append(time.plusHours(9).getHour())
                    .append(" up")
                    .append(up)
                    .append(" ")
                    .append(upRatio)
                    .append(" \tdown")
                    .append(down)
                    .append(" ")
                    .append(downRatio)
                    .append("\t")
                    .append(up.dividedBy(down).scale(2))
                    .toString();
        }
    }
}
