/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import java.util.function.Function;

import cointoss.util.Num;

/**
 * Built-in {@link Indicator} collection.
 */
public final class Indicators {

    public static NumIndicator trendLine(Ticker ticker, int length) {
        return new TrendLine(ticker, length);
    }

    public static NumIndicator waveTrend(Ticker ticker) {
        return waveTrend(ticker, 10, 21);
    }

    public static NumIndicator waveTrend(Ticker ticker, int channelLength, int averageLength) {
        NumIndicator ap = NumIndicator.build(ticker, Tick::typicalPrice);
        NumIndicator esa = ap.ema(channelLength);
        NumIndicator d = esa.nmap(ap, (a, b) -> a.minus(b).abs()).ema(channelLength);
        NumIndicator ci = ap.nmap(esa, d, (a, b, c) -> {
            if (c.isZero()) {
                return a.minus(b);
            }
            return a.minus(b).divide(Num.of(0.015).multiply(c));
        });
        return ci.ema(averageLength).scale(2);
    }

    /**
     * 
     */
    private static class TrendLine extends NumIndicator {

        private final Ticker ticker;

        private final int length;

        /** The current key. */
        private Tick latest;

        /**
         * @param ticker
         */
        private TrendLine(Ticker ticker, int length) {
            super(ticker);

            this.ticker = ticker;
            this.length = length;

            ticker.open.to(() -> {
                System.out.println("reset");
                latest = null; // reset
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            Function<Tick, Num> equation = computeEquation();

            return tick.lowPrice;
        }

        /** CACHE */
        private Function<Tick, Num> equation;

        private synchronized Function<Tick, Num> computeEquation() {
            if (equation == null) {
                equation = e -> Num.ZERO;
                System.out.println("Compute");
            }
            return equation;
        }
    }
}
