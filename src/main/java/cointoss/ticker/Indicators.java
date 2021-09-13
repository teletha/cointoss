/*
 * Copyright (C) 2021 cointoss Development Team
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
        return Indicator.build(ticker, tick -> new double[] {tick.openPrice.doubleValue(), tick.highPrice.doubleValue(),
                tick.lowPrice.doubleValue(), tick.closePrice().doubleValue()});
    }

    public static Indicator<double[]> ohclHeikinAshi(Ticker ticker) {
        return ohclCandle(ticker).memoize(24, (tick, self) -> {
            Tick before = ticker.ticks.before(tick);
            if (before == null) {
                return new double[] {tick.openPrice.doubleValue(), tick.highPrice.doubleValue(), tick.lowPrice.doubleValue(),
                        tick.closePrice().doubleValue()};
            }

            double[] prev = self.apply(before);

            return new double[] {(prev[0] + prev[3]) / 2, tick.highPrice.doubleValue(), tick.lowPrice.doubleValue(),
                    tick.heikinDoublePrice()};
        });
    }

    public static NumIndicator trend(Ticker ticker, int length) {
        return new Trend(ticker, length);
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