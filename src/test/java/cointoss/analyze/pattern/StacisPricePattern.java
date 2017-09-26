/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze.pattern;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cointoss.BackTester;
import cointoss.Side;
import cointoss.Trading;
import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;

/**
 * @version 2017/09/20 2:36:26
 */
public class StacisPricePattern extends Trading {

    private List<Statistics> statictics = new LinkedList();

    private List<Statistics> completed = new ArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.minute1.signal().to(tick -> {
            if (market.minute1.ticks.size() < 15) {
                return;
            }

            Iterator<Statistics> iterator = statictics.iterator();

            while (iterator.hasNext()) {
                Statistics statistics = iterator.next();

                if (statistics.isActive(tick.start)) {
                    // 観測中なのでスキップ
                    return;
                } else {
                    // complete
                    iterator.remove();
                    completed.add(statistics);
                    statistics.finish(tick);
                }
            }

            // 過去N分間最高値または最低値が一定の範囲におさまっている場合の検知
            int threshold = 100;
            int time = 6;
            List<Num> max = new ArrayList();
            List<Num> min = new ArrayList();

            for (int i = 0; i <= time; i++) {
                Tick latest = market.minute1.ticks.latest(i);

                max.add(Num.max(latest.openPrice, latest.closePrice));
                min.add(Num.min(latest.openPrice, latest.closePrice));
            }

            boolean maxIsStatis = max.stream().allMatch(e -> max.get(0).minus(e).abs().isLessThan(threshold));
            boolean minIsStatis = min.stream().allMatch(e -> min.get(0).minus(e).abs().isLessThan(threshold));

            if (maxIsStatis && !minIsStatis) {
                // price will be down
                statictics.add(new Statistics(tick.start, tick.closePrice, Side.SELL));
            } else if (minIsStatis && !maxIsStatis) {
                // price will be up
                statictics.add(new Statistics(tick.start, tick.closePrice, Side.BUY));
            } else {
                // unknown
            }
        });
    }

    /**
     * Analyze
     * 
     * @param args
     */
    public static void main(String[] args) {
        StacisPricePattern analyzer = new StacisPricePattern();

        BackTester.with()
                .baseCurrency(1000000)
                .targetCurrency(0)
                .log(BitFlyer.FX_BTC_JPY.log().rangeAll())
                .strategy(() -> analyzer)
                .trial(1)
                .run();

        int total = 0;
        int success = 0;
        int fail = 0;

        for (Statistics statistics : analyzer.completed) {
            total++;
            if (statistics.isSuccess()) {
                success++;
            } else {
                fail++;
            }
        }
        System.out.println("総計" + total + "   成功" + success + "  失敗" + fail + "  (" + Num.of(success)
                .divide(total)
                .multiply(100)
                .scale(1) + ")");
    }

    /**
     * @version 2017/09/20 15:36:57
     */
    private static class Statistics {

        public final ZonedDateTime start;

        public final ZonedDateTime end;

        public final Side prediction;

        public final Num startPrice;

        public Num endPrice;

        /**
         * @param start
         * @param startPrice
         */
        public Statistics(ZonedDateTime start, Num startPrice, Side prediction) {
            this.start = start;
            this.startPrice = startPrice;
            this.prediction = prediction;
            this.end = start.plusMinutes(5);
        }

        /**
         * @return
         */
        private boolean isSuccess() {
            if (prediction.isBuy()) {
                return endPrice.isGreaterThan(startPrice);
            } else {
                return startPrice.isGreaterThan(endPrice);
            }
        }

        /**
         * @param tick
         */
        private void finish(Tick tick) {
            endPrice = tick.closePrice;
        }

        private boolean isActive(ZonedDateTime now) {
            return now.isBefore(end);
        }
    }
}
