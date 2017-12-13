/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;

/**
 * @version 2017/09/19 23:45:54
 */
public class BackTest {

    /**
     * Run back test.
     * 
     * @param args
     */
    public static void main(String[] args) {
        BackTester.with()
                .baseCurrency(1000000)
                .targetCurrency(0)
                .log(() -> BitFlyer.FX_BTC_JPY.log().rangeRandom(1))
                .strategy(() -> new BuyAndHold())
                .trial(1)
                .run();
    }

    /**
     * <pre>
    発注 最小78:29:37   最大147:57:45 平均91:39:52
    保持 最小78:29:36   最大147:56:45 平均91:38:36
    損失 最小   -406円   最大    -58円  平均   -182円  合計        -547円
    利益 最小     26円   最大    227円  平均    116円  合計       3,830円
    取引 最小   -406円   最大    227円  平均     91円  合計       3,283円 (勝率91.7%  PF7 DD0.1% 総36 済0 残36 中止0)
    開始 06/21 00:00  1,000,000円  B0.0000(297,854円)   総計 1,000,000円
    終了 07/01 00:00    881,672円  B0.4318(281,633円)   総計 1,003,283円 (損益 3,283円)
    
    発注 最小78:29:44   最大147:57:36 平均91:39:52
    保持 最小78:29:37   最大147:56:45 平均91:38:36
    損失 最小   -406円   最大    -58円  平均   -182円  合計        -547円
    利益 最小     26円   最大    227円  平均    116円  合計       3,830円
    取引 最小   -406円   最大    227円  平均     91円  合計       3,283円 (勝率91.7%  PF7 DD0.1% 総36 済0 残36 中止0)
    開始 06/21 00:00  1,000,000円  B0.0000(297,854円)   総計 1,000,000円
    終了 07/01 00:00    881,672円  B0.4318(281,633円)   総計 1,003,283円 (損益 3,283円)
    
    発注 最小71:59:52   最大192:26:38 平均157:46:17
    保持 最小71:59:50   最大192:26:37 平均157:29:15
    損失 最小    -20円   最大    -20円  平均    -20円  合計         -20円
    利益 最小    126円   最大  1,245円  平均    349円  合計      57,208円
    取引 最小    -20円   最大  1,245円  平均    347円  合計      57,188円 (勝率99.4%  PF2860.391 DD0% 総165 済0 残165 中止0)
    開始 05/24 00:00  1,000,000円  B0.0000(295,014円)   総計 1,000,000円
    終了 06/03 00:00    577,814円  B1.7244(277,999円)   総計 1,057,188円 (損益 57,188円)
    
    発注 最小00:28:08   最大156:05:17 平均90:17:02
    保持 最小00:28:03   最大155:49:14 平均90:12:02
    損失 最小 -1,299円   最大     -0円  平均   -174円  合計     -11,133円
    利益 最小      0円   最大    345円  平均    169円  合計      24,030円
    取引 最小 -1,299円   最大    345円  平均     63円  合計      12,897円 (勝率68.9%  PF2.158 DD1.1% 総206 済0 残206 中止0)
    開始 06/09 00:00  1,000,000円  B0.0000(308,990円)   総計 1,000,000円
    終了 06/19 00:00    395,836円  B2.1553(286,299円)   総計 1,012,897円 (損益 12,897円)
    
    発注 最小24:03:56   最大144:30:47 平均109:37:46
    保持 最小24:03:49   最大144:30:42 平均109:33:29
    損失 最小   -745円   最大     -7円  平均   -182円  合計        -910円
    利益 最小      7円   最大    590円  平均    221円  合計      34,009円  
    取引 最小   -745円   最大    590円  平均    208円  合計      33,099円 (勝率96.9%  PF37.379 DD0.1% 総159 済0 残159 中止0)
    開始 05/22 00:00  1,000,000円  B0.0000(261,179円)   総計 1,000,000円
    終了 06/01 00:00    582,301円  B1.7011(265,000円)   総計 1,033,099円 (損益 33,099円)
    </pre>
     */
    private static class BuyAndHold extends Trading {

        public String decreaseRatio = "0.95";

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            market.minute1.signal().takeAt(i -> i % 10 == 0).to(tick -> {
                System.out.println(tick);
            });
        }
    }

    private static class SellAndHold extends Trading {

        private Num budget = Num.ZERO;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            market.minute1.signal().takeAt(i -> i % 10 == 0).to(exe -> {
                budget = budget.plus(Num.of(10000 / (24 * 12)));

                Tick tick = market.day1.ticks.latest(1);

                if (tick != null && exe.closePrice.isGreaterThan(tick.minPrice.multiply(Num.of(1.07)))) {
                    Num price = market.getExecutionLatest().price;
                    Num size = Num.of(0.01);

                    entryLimit(Side.SELL, size, price, entry -> {
                        budget = Num.ZERO;
                    });
                }
            });
        }
    }

    /**
     * @version 2017/09/05 20:19:04
     */
    private static class BreakoutTrading extends Trading {

        Num profitPrice;

        Num lossPrice;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {

        }
    }
}
