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

import org.apache.commons.math3.stat.regression.SimpleRegression;

import cointoss.util.Num;

/**
 * Built-in {@link Indicator} collection.
 */
public final class Indicators {

    public static NumIndicator trend(Ticker ticker, int length) {
        return new Trend(ticker, length);
    }

    public static NumIndicator lowTrendLine(Ticker ticker, int length) {
        return new LowTrendLine(ticker, length);
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
        NumIndicator indi = ci.ema(averageLength).scale(2);
        indi.name.set("WaveTrend " + ticker.span.name());
        return indi;
    }

    /**
     * 
     */
    private static class LowTrendLine extends NumIndicator {

        private final Ticker ticker;

        private final int length;

        private final SimpleRegression regression = new SimpleRegression(true);

        private long minTime = 0;

        private double minValue = Double.MAX_VALUE;

        /**
         * @param ticker
         */
        private LowTrendLine(Ticker ticker, int length) {
            super(ticker);

            this.ticker = ticker;
            this.length = length;

            ticker.ticks.each(this::add);
            ticker.close.to(this::add);
        }

        private void add(Tick tick) {
            double low = tick.closePrice().doubleValue();

            regression.addData(tick.startSeconds, low);

            if (low < minValue) {
                minValue = low;
                minTime = tick.startSeconds;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            return Num.of(regression.predict(tick.startSeconds));
        }
    }

    /**
     * 
     */
    private static class HighTrendLine extends NumIndicator {

        private final Ticker ticker;

        private final int length;

        private final SimpleRegression regression = new SimpleRegression(true);

        private long maxTime = 0;

        private double maxValue = Double.MIN_VALUE;

        /**
         * @param ticker
         */
        private HighTrendLine(Ticker ticker, int length) {
            super(ticker);

            this.ticker = ticker;
            this.length = length;

            ticker.ticks.each(this::add);
            ticker.close.to(this::add);
        }

        private void add(Tick tick) {
            double high = tick.highPrice.doubleValue();

            regression.addData(tick.startSeconds, high);

            if (maxValue < high) {
                maxValue = high;
                maxTime = tick.startSeconds;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            return Num.of(regression.predict(tick.startSeconds) + (maxValue - regression.predict(maxTime)));
        }
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
