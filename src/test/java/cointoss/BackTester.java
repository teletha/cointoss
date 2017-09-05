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
import cointoss.market.bitflyer.BitFlyerBTCFXBuilder;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
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
    private Decimal base = Decimal.ZERO;

    /** 対象通貨量 */
    private Decimal target = Decimal.ZERO;

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
        public Signal<Ⅱ<Decimal, Decimal>> getCurrency() {
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

        private final Decimal size = Decimal.valueOf("1");

        private final Decimal lossLimit = Decimal.valueOf("7000");

        private final Decimal interval = Decimal.valueOf("100");

        private Indicator<Decimal> longMV;

        private Indicator<Decimal> shortMV;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(Market market) {
            Indicator<Decimal> closer = new ClosePriceIndicator(market.series);
            shortMV = new SMAIndicator(closer, 12);
            longMV = new SMAIndicator(closer, 60);
        }

        private void exit(OrderAndExecution entry, Decimal size, Decimal base, Decimal limitLine, Market market, int count) {
            Order.market(entry.inverse(), size).when(limitLine).with(entry).entryTo(market).to(exit -> {
                Decimal diff = exit.e.price.minus(shortMV.getValue(shortMV.getTimeSeries().getEnd()));

                if (exit.isBuy() ? diff.isLessThan(-1800) : diff.isGreaterThan(1800)) {
                    market.cancel(exit.o);
                    Order.market(exit.side(), exit.o.outstanding_size).with(entry).entryTo(market).to();
                } else if (exit.e.price.isGreaterThan(entry, base.plus(entry, interval))) {
                    market.cancel(exit.o);
                    exit(entry, exit.o.outstanding_size, exit.e.price, exit.e.price
                            .minus(entry, lossLimit.minus(Decimal.of(50).multiply(count))), market, count + 1);
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNoPosition(Market market, Execution exe) {
            if (market.hasNoActiveOrder()) {
                Decimal diff = market.series.getLastTick().getClosePrice().minus(longMV.getValue(longMV.getTimeSeries().getEnd()));

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
