/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.temporal.ChronoUnit;

import cointoss.backtest.BackTester;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.Tick;
import cointoss.ticker.TickSpan;
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
                .strategy(() -> new BreakoutTrading())
                .trial(1)
                .run();
    }

    /**
     * @version 2017/09/05 20:19:04
     */
    private static class BreakoutTrading extends Trader {

        private int update;

        private Num underPrice;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            // various events
            market.timeline.to(exe -> {
                if (hasPosition() == false) {
                    Entry latest = latest();
                    Side side;

                    if (latest == null) {
                        side = Side.random();
                    } else {
                        side = latest.isWin() ? latest.side() : latest.inverse();
                    }

                    entryMarket(side, maxPositionSize, entry -> {
                        update = 1;
                        underPrice = exe.price.minus(entry, 4000);

                        // cancel timing
                        market.timeline.takeUntil(completingEntry)
                                .take(keep(5, ChronoUnit.MINUTES, entry.order::isNotCompleted))
                                .take(1)
                                .mapTo(entry.order)
                                .to(t -> {
                                    System.out.println("cancel " + entry.order);
                                    cancel(entry);
                                });

                        // rise under price line
                        market.tickerBy(TickSpan.Second10).add.takeUntil(closingPosition) //
                                .map(Tick::getClosePrice)
                                .takeAt(i -> i % 5 == 0)
                                .to(e -> {
                                    Num next = e.minus(entry, Math.max(0, 4000 - update * 200));

                                    if (next.isGreaterThan(entry, underPrice)) {
                                        entry.log("最低価格を%sから%sに再設定 参考値%s", underPrice, next, e);
                                        update++;
                                        underPrice = next;
                                    }
                                });

                        // loss cut
                        market.timeline.takeUntil(closingPosition) //
                                .take(keep(5, ChronoUnit.SECONDS, e -> e.price.isLessThan(entry, underPrice)))
                                .take(1)
                                .to(e -> {
                                    entry.exitLimit(entry.entrySize(), underPrice, exit -> {
                                        entry.log("10秒以上約定値が%s以下になったので指値で決済開始", underPrice);

                                        market.timeline.takeUntil(completingEntry)
                                                .take(keep(30, ChronoUnit.SECONDS, exit::isNotCompleted))
                                                .take(1)
                                                .to(x -> {
                                                    market.cancel(exit).to(() -> {
                                                        entry.log("30秒待っても処理されないので指値をキャンセルして成行決済 " + exit.remainingSize);
                                                        // entry.exitMarket(exit.outstanding_size);
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
