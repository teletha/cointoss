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

import java.time.ZonedDateTime;

import cointoss.analyze.OnlineStats;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;

public final class Tick {

    /** The empty dummy. */
    static final Tick EMPTY = new Tick();

    /** Begin time of this tick (epoch second). */
    public final long openTime;

    /** Open price of the period */
    public final Num openPrice;

    /** Close price of the period. */
    Num closePrice;

    /** Max price of the period */
    Num highPrice;

    /** Min price of the period */
    Num lowPrice;

    /** Snapshot of long volume at tick initialization. */
    double longVolume;

    /** Snapshot of long losscut volume at tick initialization. */
    double longLosscutVolume;

    /** Snapshot of short volume at tick initialization. */
    double shortVolume;

    /** Snapshot of short losscut volume at tick initialization. */
    double shortLosscutVolume;

    /** The trend type. */
    Trend trend;

    /** The source ticker. */
    Ticker ticker;

    /**
     * Empty Dummt Tick.
     */
    private Tick() {
        this.openTime = 0;
        this.openPrice = closePrice = highPrice = lowPrice = Num.ZERO;
    }

    /**
     * New {@link Tick}.
     * 
     * @param openTime A start time of period.
     * @param open A open price.
     * @param ticker The data source.
     */
    Tick(long startEpochSeconds, Num open, Ticker ticker) {
        this.openTime = startEpochSeconds;
        this.openPrice = this.highPrice = this.lowPrice = open;

        this.ticker = ticker;
        this.longVolume = ticker.realtime.longVolume;
        this.longLosscutVolume = ticker.realtime.longLosscutVolume;
        this.shortVolume = ticker.realtime.shortVolume;
        this.shortLosscutVolume = ticker.realtime.shortLosscutVolume;
    }

    /**
     * Retrieve the start time of this {@link Tick}.
     * 
     * @return The start time.
     */
    public ZonedDateTime openTime() {
        return Chrono.utcByMills(openTime * 1000);
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num openPrice() {
        return openPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num closePrice() {
        return ticker == null ? closePrice : ticker.realtime.latest.v.price;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num highPrice() {
        return highPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num lowPrice() {
        return lowPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num upperPrice() {
        Num close = closePrice();
        return openPrice.isLessThan(close) ? close : openPrice;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public Num lowerPrice() {
        Num close = closePrice();
        return openPrice.isLessThan(close) ? openPrice : close;
    }

    /**
     * Typical price (sometimes called the pivot point) refers to the arithmetic average of the
     * high, low, and closing prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public Num typicalPrice() {
        return highPrice.plus(lowPrice).plus(closePrice()).divide(Num.THREE);
    }

    /**
     * Typical price (sometimes called the pivot point) refers to the arithmetic average of the
     * high, low, and closing prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public double typicalDoublePrice() {
        return (highPrice.doubleValue() + lowPrice.doubleValue() + closePrice().doubleValue()) / 3;
    }

    /**
     * Median price (sometimes called the high-low price) refers to the arithmetic average of the
     * high and low prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public Num medianPrice() {
        return highPrice.plus(lowPrice).divide(Num.TWO);
    }

    /**
     * Median price (sometimes called the high-low price) refers to the arithmetic average of the
     * high and low prices for this {@link Tick}.
     * 
     * @return The tick related value.
     */
    public double medianDoublePrice() {
        return (highPrice.doubleValue() + lowPrice.doubleValue()) / 2;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double volume() {
        return longVolume() + shortVolume();
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double longVolume() {
        return ticker == null ? longVolume : ticker.realtime.longVolume - longVolume;
    }

    /**
     * Retrieve the tick related value.
     */
    public double longLosscutVolume() {
        return ticker == null ? longLosscutVolume : ticker.realtime.longLosscutVolume - longLosscutVolume;
    }

    /**
     * Retrieve the tick related value.
     * 
     * @return The tick related value.
     */
    public double shortVolume() {
        return ticker == null ? shortVolume : ticker.realtime.shortVolume - shortVolume;
    }

    /**
     * Retrieve the tick related value.
     */
    public double shortLosscutVolume() {
        return ticker == null ? shortLosscutVolume : ticker.realtime.shortLosscutVolume - shortLosscutVolume;
    }

    /**
     * Compute the spread in prices.
     * 
     * @return
     */
    public Num spread() {
        return highPrice().minus(lowPrice());
    }

    /**
     * Compute the spread in prices.
     * 
     * @return
     */
    public double spreadDouble() {
        return highPrice().doubleValue() - lowPrice().doubleValue();
    }

    /**
     * Detect the trend type at this {@link Tick}.
     * 
     * @return
     */
    public Trend trend() {
        return ticker == null ? trend : new TrendDetector(this).detect();
    }

    /**
     * Make this {@link Tick}'s related values fixed.
     * 
     * @return
     */
    void freeze() {
        ticker.spreadStats.add(spreadDouble());
        ticker.volumeStats.add(volume());
        ticker.typicalStats.add(typicalDoublePrice());
        trend = new TrendDetector(this).detect();

        closePrice = closePrice();
        longVolume = longVolume();
        longLosscutVolume = longLosscutVolume();
        shortVolume = shortVolume();
        shortLosscutVolume = shortLosscutVolume();
        ticker = null;
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBull() {
        return openPrice.isLessThan(closePrice());
    }

    /**
     * Check the tick state.
     * 
     * @return
     */
    public boolean isBear() {
        return openPrice.isGreaterThan(closePrice());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TICK ").append(" ")
                .append(Chrono.format(openTime()))
                .append(" ")
                .append(openPrice)
                .append(" ")
                .append(closePrice())
                .append(" ")
                .append(highPrice)
                .append(" ")
                .append(lowPrice)
                .append(" ")
                .append(longVolume())
                .append(" ")
                .append(shortVolume());

        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.hashCode(openTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tick == false) {
            return false;
        }

        Tick other = (Tick) obj;
        return openTime == other.openTime;
    }

    /**
     * Estimate trend type from {@link Tick}.
     */
    static class TrendDetector {

        private int rangeOrTrend;

        private int buyOrSell;

        private final Tick tick;

        private final Tick prev;

        private final OnlineStats volumes;

        private final OnlineStats spreds;

        private final OnlineStats typicals;

        /**
         * @param tick
         */
        TrendDetector(Tick tick) {
            this.tick = tick;
            this.prev = tick.ticker.ticks.before(tick);
            this.volumes = tick.ticker.volumeStats;
            this.spreds = tick.ticker.spreadStats;
            this.typicals = tick.ticker.typicalStats;

            if (prev != null) {
                volumeSizeOutlier();
                volumeSideRatio();
                volumeAction();
                volatilityOutlier();
                priceAction();
            }
        }

        /**
         * Check abnormal volume size.
         */
        private void volumeSizeOutlier() {
            if (volumes.calculateSigma(tick.volume()) <= 1) {
                rangeOrTrend++;
            } else {
                rangeOrTrend--;
            }
        }

        /**
         * Check volume side.
         */
        private void volumeSideRatio() {
            double delta = tick.longVolume - tick.shortVolume;
            if (volumes.getStdDev() < Math.abs(delta)) {
                rangeOrTrend--;

                if (0 < delta) {
                    buyOrSell++;
                } else {
                    buyOrSell--;
                }
            } else {
                rangeOrTrend++;
            }
        }

        private void volumeAction() {
            double delta = tick.volume() - prev.volume();
            if (volumes.getStdDev() < Math.abs(delta)) {
                if (0 < delta) {
                    rangeOrTrend++;
                } else {
                    rangeOrTrend--;
                }
            }
        }

        /**
         * Check abnormal volatility.
         */
        private void volatilityOutlier() {
            if (spreds.calculateSigma(tick.spreadDouble()) <= 1) {
                rangeOrTrend++;
            } else {
                rangeOrTrend--;
            }
        }

        private void priceAction() {
            comparePrice(prev.highPrice, tick.highPrice);
            comparePrice(prev.lowPrice, tick.lowPrice);
            comparePrice(prev.upperPrice(), tick.upperPrice());
            comparePrice(prev.lowerPrice(), tick.lowerPrice());
        }

        private void comparePrice(Num prev, Num now) {
            double typical = 0;
            double prevPrice = prev.doubleValue();
            double nowPrice = now.doubleValue();

            if (typical < nowPrice - prevPrice) {
                rangeOrTrend--;
                buyOrSell++;
            } else if (prevPrice - nowPrice > typical) {
                rangeOrTrend--;
                buyOrSell--;
            } else {
                rangeOrTrend++;
            }
        }

        private Trend detect() {
            if (prev != null) {
                if (2 <= rangeOrTrend) {
                    return Trend.Range;
                } else if (rangeOrTrend <= -2) {
                    if (1 <= buyOrSell) {
                        return Trend.Buy;
                    } else if (buyOrSell <= -1) {
                        return Trend.Sell;
                    }
                }
            }
            return Trend.Unknown;
        }
    }
}