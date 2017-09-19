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

import java.util.stream.IntStream;

import cointoss.Time.Lag;
import cointoss.analyze.TradingLog;
import cointoss.chart.Tick;
import cointoss.market.bitflyer.BitFlyer;
import eu.verdelhan.ta4j.Decimal;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/07/24 20:56:39
 */
public class BackTester {

    /** 試行回数 */
    private int trial = 5;

    /** 基軸通貨量 */
    private Decimal base = Decimal.ZERO;

    /** 対象通貨量 */
    private Decimal target = Decimal.ZERO;

    /** テスト対象マーケット */
    private MarketLog marketLog;

    /** テスト戦略 */
    private Class<? extends Trading> strategy;

    /** ラグ生成器 */
    private Lag lag = Time.lag(2, 15);

    /**
     * Hide
     */
    private BackTester() {
    }

    /**
     * Set the test strategy.
     * 
     * @param strategy
     * @return
     */
    public BackTester strategy(Class<? extends Trading> strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
        return this;
    }

    /**
     * Set a number of trial.
     * 
     * @param number
     * @return
     */
    public BackTester trial(int number) {
        if (0 < number) {
            this.trial = number;
        }
        return this;
    }

    /**
     * Set initial balance.
     * 
     * @param base
     * @param target
     * @return
     */
    public BackTester balance(int base, int target) {
        return balance(Decimal.of(base), Decimal.of(target));
    }

    /**
     * Set initial balance.
     * 
     * @param base
     * @param target
     * @return
     */
    public BackTester balance(Decimal base, Decimal target) {
        if (base.isGreaterThanOrEqual(0) && target.isLessThanOrEqual(0)) {
            this.base = base;
            this.target = target;
        }
        return this;
    }

    /**
     * Execute back test.
     */
    public void execute() {
        IntStream.range(0, trial)
                .parallel()
                .mapToObj(i -> new Market(new BackTestBackend(), marketLog.rangeRandom(7 * 4), strategy))
                .forEach(market -> {
                    System.out.println(new TradingLog(market, market.tradings));
                });
    }

    /**
     * <p>
     * Create new back tester.
     * </p>
     * 
     * @return
     */
    public static BackTester initialize(MarketLog marketLog) {
        BackTester tester = new BackTester();
        tester.marketLog = marketLog;

        return tester;
    }

    /**
     * @version 2017/08/16 9:16:09
     */
    private class BackTestBackend extends TestableMarketBackend {

        /**
         */
        private BackTestBackend() {
            super(lag);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Signal<BalanceUnit> getCurrency() {
            BalanceUnit base = new BalanceUnit();
            base.currency_code = "JPY";
            base.amount = base.available = BackTester.this.base;

            BalanceUnit target = new BalanceUnit();
            target.currency_code = "BTC";
            target.amount = target.available = BackTester.this.target;

            return I.signal(base, target);
        }
    }

    /**
     * Run back test.
     * 
     * @param args
     */
    public static void main(String[] args) {
        BackTester tester = BackTester.initialize(BitFlyer.FX_BTC_JPY.log()).balance(1000000, 0).strategy(BuyAndHold.class);
        tester.execute();
    }

    /**
     * @version 2017/09/19 17:44:44
     */
    private static class BuyAndHold extends Trading {

        private Decimal budget = Decimal.ZERO;

        /**
         * @param market
         */
        private BuyAndHold(Market market) {
            super(market);

            market.minute5.to(exe -> {
                budget = budget.plus(Decimal.valueOf(10000 / (24 * 12)));

                Tick tick = market.hour12.ticks.latest(0);

                if (tick != null && exe.closePrice.isLessThan(tick.maxPrice.multipliedBy(Decimal.valueOf(0.9)))) {
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
