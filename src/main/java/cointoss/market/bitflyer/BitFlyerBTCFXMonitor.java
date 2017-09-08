/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.Execution;
import cointoss.Market;
import cointoss.Trading;
import eu.verdelhan.ta4j.Decimal;
import kiss.I;

/**
 * @version 2017/09/07 12:02:46
 */
public class BitFlyerBTCFXMonitor {

    /**
     * @version 2017/09/07 12:35:08
     */
    private static class TradingMonitor extends Trading {

        /**
         * @param market
         */
        public TradingMonitor(Market market) {
            super(market);

            market.observeExecutionBySize(20).to(exe -> {
                System.out.println("大口 " + exe.side.mark() + exe.cumulativeSize);
            });

            market.minute1.to(tick -> {
                System.out.println(tick);
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryEntry(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryExit(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void timeline(Execution exe) {
        }
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        I.load(Decimal.Codec.class, false);

        Market market = new Market(new BitFlyerBTCFX(), BitFlyer.FX_BTC_JPY.log().fromToday(), TradingMonitor.class);
    }
}
