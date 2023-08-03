/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import cointoss.util.arithmetic.Num;

/**
 * Built-in {@link Indicator} collection.
 */
public final class Indicators {

    public static Indicator<double[]> ohclCandle(Ticker ticker) {
        return Indicator.build(ticker, tick -> new double[] {tick.openPrice, tick.highPrice, tick.lowPrice, tick.closePrice()});
    }

    public static Indicator<double[]> ohclHeikinAshi(Ticker ticker) {
        return ohclCandle(ticker).memoize(24, (tick, self) -> {
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return new double[] {tick.openPrice, tick.highPrice, tick.lowPrice, tick.closePrice()};
            }

            double[] prev = self.apply(before);

            return new double[] {(prev[0] + prev[3]) / 2, tick.highPrice, tick.lowPrice, tick.heikinPrice()};
        });
    }

    public static NumIndicator trend(Ticker ticker, int length) {
        return new Trend(ticker, length);
    }

    /**
     * 
     */
    private static class Trend extends NumIndicator {

        private final Ticker ticker;

        private final int length;

        /**
         * @param ticker
         */
        private Trend(Ticker ticker, int length) {
            super(ticker);

            this.ticker = ticker;
            this.length = length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            return Num.ZERO;
        }
    }
}