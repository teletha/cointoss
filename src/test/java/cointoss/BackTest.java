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

import static java.time.temporal.ChronoUnit.*;

import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import eu.verdelhan.ta4j.Decimal;

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
                .log(() -> BitFlyer.FX_BTC_JPY.log().rangeRandom(7))
                .strategy(BuyAndHold.class)
                .trial(5)
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

        private Decimal budget = Decimal.ZERO;

        private BuyAndHold(Market market) {
            super(market);

            market.minute1.signal().takeAt(i -> i % 10 == 0).to(exe -> {
                budget = budget.plus(Decimal.valueOf(10000 / (24 * 12)));

                Tick tick = market.day1.ticks.latest(1);

                if (tick != null && exe.closePrice.isLessThan(tick.closePrice.multipliedBy(Decimal.valueOf(0.9)))) {
                    Decimal price = market.getExecutionLatest().price;
                    Decimal size = budget.dividedBy(price).max(Decimal.valueOf(0.01));

                    entryLimit(Side.BUY, size, price, entry -> {
                        budget = Decimal.ZERO;
                    });
                }
            });
        }
    }

    /**
     * @version 2017/09/05 20:19:04
     */
    private static class BreakoutTrading extends Trading {

        private int update;

        private Decimal underPrice;

        /**
         * @param market
         * @param exe
         */
        private BreakoutTrading(Market market) {
            super(market);

            // various events
            market.timeline.to(exe -> {
                if (hasPosition() == false) {
                    Entry latest = latest();
                    Side side;

                    if (latest == null) {
                        side = Side.random();
                    } else {
                        if (market.minute5.isRange()) {
                            side = latest.isWin() ? latest.inverse() : latest.side();
                        } else {
                            side = latest.isWin() ? latest.side() : latest.inverse();
                        }
                    }

                    entryMarket(side, maxPositionSize, entry -> {
                        update = 1;
                        underPrice = exe.price.minus(entry, 4000);

                        // cancel timing
                        market.timeline.takeUntil(completingEntry)
                                .take(keep(5, MINUTES, entry.order::isNotCompleted))
                                .take(1)
                                .mapTo(entry.order)
                                .to(t -> {
                                    System.out.println("cancel " + entry.order);
                                    cancel(entry);
                                });

                        // rise under price line
                        market.second10.tick.takeUntil(closingPosition) //
                                .map(Tick::getClosePrice)
                                .takeAt(i -> i % 5 == 0)
                                .to(e -> {
                                    Decimal next = e.minus(entry, Math.max(0, 4000 - update * 200));

                                    if (next.isGreaterThan(entry, underPrice)) {
                                        entry.log("最低価格を%sから%sに再設定 参考値%s", underPrice, next, e);
                                        update++;
                                        underPrice = next;
                                    }
                                });

                        // loss cut
                        market.timeline.takeUntil(closingPosition) //
                                .take(keep(5, SECONDS, e -> e.price.isLessThan(entry, underPrice)))
                                .take(1)
                                .to(e -> {
                                    entry.exitLimit(entry.entrySize(), underPrice, exit -> {
                                        entry.log("10秒以上約定値が%s以下になったので指値で決済開始", underPrice);

                                        market.timeline.takeUntil(completingEntry)
                                                .take(keep(30, SECONDS, exit::isNotCompleted))
                                                .take(1)
                                                .to(x -> {
                                                    market.cancel(exit).to(() -> {
                                                        entry.log("30秒待っても処理されないので指値をキャンセルして成行決済 " + exit.outstanding_size);
                                                        entry.exitMarket(exit.outstanding_size);
                                                    });
                                                });
                                    });
                                });
                    });
                }
            });
        }
    }
}
