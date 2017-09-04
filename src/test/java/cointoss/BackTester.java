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

import java.util.stream.IntStream;

import cointoss.Time.Lag;
import cointoss.indicator.Indicator;
import cointoss.indicator.simple.PriceIndicator;
import cointoss.indicator.trackers.SimpleMovingAverageIndicator;
import cointoss.market.bitflyer.BitFlyerBTCFXBuilder;
import kiss.I;
import kiss.Signal;
import kiss.Ⅱ;

/**
 * @version 2017/07/24 20:56:39
 */
public class BackTester {

    /** 試行回数 */
    private int trial = 3;

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

        private final Amount lossLimit = new Amount("7000");

        private final Amount interval = new Amount("100");

        private Indicator<Amount> longMV;

        private Indicator<Amount> shortMV;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(Market market) {
            Indicator<Amount> closer = new PriceIndicator(market.ticks, Tick::getMiddle);
            shortMV = new SimpleMovingAverageIndicator(closer, 12);
            longMV = new SimpleMovingAverageIndicator(closer, 60);
        }

        private void exit(OrderAndExecution entry, Amount size, Amount base, Amount limitLine, Market market, int count) {
            Order.market(entry.inverse(), size).when(limitLine).with(entry).entryTo(market).to(exit -> {
                Amount diff = exit.e.price.minus(shortMV.getLast());

                if (exit.isBuy() ? diff.isLessThan(-1800) : diff.isGreaterThan(1800)) {
                    market.cancel(exit.o);
                    Order.market(exit.side(), exit.o.outstanding_size).with(entry).entryTo(market).to();
                } else if (exit.e.price.isGreaterThan(entry, base.plus(entry, interval))) {
                    market.cancel(exit.o);
                    exit(entry, exit.o.outstanding_size, exit.e.price, exit.e.price
                            .minus(entry, lossLimit.minus(Amount.of(50).multiply(count))), market, count + 1);
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNoPosition(Market market, Execution exe) {
            if (60 < market.ticks.size() && market.hasNoActiveOrder()) {
                Amount diff = market.ticks.getLastTick().closing.minus(longMV.getLast());

                if (diff.abs().isGreaterThan(5200)) {
                    Side side = diff.isNegative() ? Side.BUY : Side.SELL;

                    Order.market(side, size) //
                            .entryTo(market)
                            .to(entry -> {
                                if (entry.e.isMine()) {
                                    exit(entry, entry.e.size, entry.e.price, entry.e.price.minus(entry, lossLimit), market, 1);
                                }
                            });
                }
            }
        }
    }
}
