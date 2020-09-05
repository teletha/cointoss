/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import cointoss.util.arithmetic.Num;

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

    public static DoubleIndicator waveTrend(Ticker ticker) {
        return waveTrend(ticker, 10, 21);
    }

    public static DoubleIndicator waveTrend(Ticker ticker, int channelLength, int averageLength) {
        DoubleIndicator price = DoubleIndicator.build(ticker, Tick::typicalDoublePrice);
        DoubleIndicator priceEMA = price.ema(channelLength);
        DoubleIndicator emaOnDiffPriceAndPriceEMA = priceEMA.dmap(price, (pEMA, p) -> Math.abs(pEMA - p)).ema(channelLength);
        DoubleIndicator ci = price.dmap(priceEMA, emaOnDiffPriceAndPriceEMA, (a, b, c) -> {
            if (c == 0) {
                return a - b;
            }
            return (a - b) / (0.015 * c);
        });
        DoubleIndicator indi = ci.ema(averageLength).scale(2);
        indi.name.set(ticker.span.toString());
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

            regression.addData(tick.openTime, low);

            if (low < minValue) {
                minValue = low;
                minTime = tick.openTime;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            return Num.of(regression.predict(tick.openTime));
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

            regression.addData(tick.openTime, high);

            if (maxValue < high) {
                maxValue = high;
                maxTime = tick.openTime;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Num valueAtRounded(Tick tick) {
            return Num.of(regression.predict(tick.openTime) + (maxValue - regression.predict(maxTime)));
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