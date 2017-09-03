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

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import cointoss.Amount;
import cointoss.Execution;
import cointoss.Market;
import cointoss.MarketBuilder;
import cointoss.Order;
import cointoss.Side;
import cointoss.Trade;
import cointoss.Time.Lag;
import cointoss.indicator.simple.ClosePriceIndicator;
import cointoss.indicator.trackers.ExponentialMovingAverageIndicator;
import cointoss.indicator.trackers.MACDIndicator;
import cointoss.market.bitflyer.BitFlyerBTCFXBuilder;
import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

/**
 * @version 2017/07/24 20:56:39
 */
public class BackTester {

    /** 試行回数 */
    private int trial = 5;

    /** 基軸通貨量 */
    private Amount base = Amount.ZERO;

    /** 対象通貨量 */
    private Amount target = Amount.ZERO;

    /** テスト対象マーケット */
    private MarketBuilder builder;

    /** テスト戦略 */
    private Class<? extends Trade> strategy;

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
    public BackTester strategy(Class<? extends Trade> strategy) {
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
        return balance(Amount.of(base), Amount.of(target));
    }

    /**
     * Set initial balance.
     * 
     * @param base
     * @param target
     * @return
     */
    public BackTester balance(Amount base, Amount target) {
        if (base.isEqualOrGreaterThan(0) && target.isEqualOrLessThan(0)) {
            this.base = base;
            this.target = target;
        }
        return this;
    }

    /**
     * Execute back test.
     */
    public void execute() {
        IntStream.range(0, trial).parallel().mapToObj(i -> new Market(new BackTestBackend(), builder, strategy)).forEach(market -> {
            market.logger.analyze();
        });
    }

    /**
     * <p>
     * Create new back tester.
     * </p>
     * 
     * @return
     */
    public static BackTester initialize(Class<? extends MarketBuilder> market) {
        BackTester tester = new BackTester();
        tester.builder = I.make(market);

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
        public Signal<Ⅱ<Amount, Amount>> getCurrency() {
            return I.signal(I.pair(base, target));
        }
    }

    /**
     * Run back test.
     * 
     * @param args
     */
    public static void main(String[] args) {
        BackTester tester = BackTester.initialize(BitFlyerBTCFXBuilder.class).balance(1000000, 0).strategy(BackTestTrade.class);
        tester.execute();
    }

    /**
     * @version 2017/08/24 23:18:21
     */
    private static class BackTestTrade extends Trade {

        private final Amount size = new Amount("1");

        private final Amount profitLimit = new Amount("3");

        private final Amount lossLimit = new Amount("2.8");

        private final long orderTimeLimit = 60 * 5;

        private final long holdTimeLimit = 60 * 60 * 24 * 1;

        private MACDIndicator macd;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(Market market) {
            ClosePriceIndicator closePrise = new ClosePriceIndicator(market.ticks);
            ExponentialMovingAverageIndicator shortEMA = new ExponentialMovingAverageIndicator(closePrise, 9);
            ExponentialMovingAverageIndicator longEMA = new ExponentialMovingAverageIndicator(closePrise, 26);
            macd = new MACDIndicator(closePrise, 9, 26);
            ExponentialMovingAverageIndicator emaMacd = new ExponentialMovingAverageIndicator(macd, 18);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNoPosition(Market market, Execution exe) {
            if (market.hasNoActiveOrder() && 60 < market.ticks.size()) {
                Side side = Side.random();
                Amount price = exe.price;
                LocalDateTime orderTime = exe.after(orderTimeLimit);
                LocalDateTime holdTime = exe.after(holdTimeLimit);

                Order.limit(side, size, price).entryTo(market).to(entry -> {
                    if (entry.isAfter(orderTime)) {
                        // cancel
                        market.cancel(entry.o).to();
                    } else if (entry.e.isMine()) {
                        // executed
                        Amount loss = price.ratio(entry.inverse(), lossLimit);

                        Order.limit(entry.inverse(), entry.e.size, price.ratio(entry, profitLimit)).with(entry).entryTo(market).to(exit -> {

                            if (exit.isAfter(holdTime)) {
                                exit.clear("時間経過");
                            } else if (exit.e.price.isLessThan(entry, loss)) {
                                exit.clear("ロスカット");
                            }
                        });
                    }
                });
            }
        }
    }
}
