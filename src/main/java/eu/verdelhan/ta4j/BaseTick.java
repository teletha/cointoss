/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eu.verdelhan.ta4j;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Execution;

/**
 * End tick of a time period.
 * <p>
 */
public class BaseTick implements Tick, Serializable {

    private static final long serialVersionUID = 8038383777467488147L;

    /** Time period (e.g. 1 day, 15 min, etc.) of the tick */
    public Duration timePeriod;

    /** End time of the tick */
    public ZonedDateTime endTime;

    /** Begin time of the tick */
    public ZonedDateTime beginTime;

    /** Open price of the period */
    public Decimal openPrice = null;

    /** Close price of the period */
    public Decimal closePrice = null;

    /** Max price of the period */
    public Decimal maxPrice = Decimal.ZERO;

    /** Min price of the period */
    public Decimal minPrice = Decimal.MAX;

    /** Traded amount during the period */
    public Decimal amount = Decimal.ZERO;

    /** Volume of the period */
    public Decimal volume = Decimal.ZERO;

    /** Trade count */
    protected int trades = 0;

    /**
     * Initialize.
     */
    public BaseTick(Duration duration) {
        this.openPrice = this.closePrice = Decimal.ZERO;
        this.beginTime = ZonedDateTime.now();
        this.endTime = beginTime.plus(duration);
    }

    /**
     * Initialize.
     * 
     * @param opening
     */
    public BaseTick(Execution opening, Duration duration) {
        this.openPrice = opening.price;
        this.beginTime = opening.exec_date.withNano(0).withSecond(0);
        this.endTime = beginTime.plus(duration);

        mark(opening);
    }

    /**
     * Initialize.
     * 
     * @param opening
     */
    public BaseTick(BaseTick opening, Duration duration) {
        this.openPrice = opening.openPrice;
        this.beginTime = opening.beginTime.withNano(0).withSecond(0);
        this.endTime = beginTime.plus(duration);

        mark(opening);
    }

    /**
     * Record {@link Execution}.
     * 
     * @param exe
     */
    public void mark(Execution exe) {
        Decimal price = exe.price;
        this.closePrice = price;
        this.maxPrice = Decimal.max(maxPrice, price);
        this.minPrice = Decimal.min(minPrice, price);

        Decimal size = exe.size;
        this.volume = volume.plus(size);
    }

    /**
     * Record {@link Execution}.
     * 
     * @param exe
     */
    public void mark(BaseTick exe) {
        this.closePrice = exe.closePrice;
        this.maxPrice = Decimal.max(maxPrice, exe.maxPrice);
        this.minPrice = Decimal.min(minPrice, exe.minPrice);
        this.volume = volume.plus(exe.volume);
    }

    /**
     * Calculate median.
     * 
     * @return
     */
    public Decimal getMedian() {
        return maxPrice.plus(minPrice).dividedBy(2);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Decimal getTypical() {
        return maxPrice.plus(minPrice).plus(closePrice).dividedBy(3);
    }

    /**
     * Calculate typical price.
     * 
     * @return
     */
    public Decimal getMiddle() {
        return openPrice.plus(closePrice).dividedBy(2);
    }

    /**
     * Constructor.
     * 
     * @param timePeriod the time period
     * @param endTime the end time of the tick period
     */
    public BaseTick(Duration timePeriod, ZonedDateTime endTime) {
        checkTimeArguments(timePeriod, endTime);
        this.timePeriod = timePeriod;
        this.endTime = endTime;
        this.beginTime = endTime.minus(timePeriod);
    }

    /**
     * Constructor.
     * 
     * @param endTime the end time of the tick period
     * @param openPrice the open price of the tick period
     * @param highPrice the highest price of the tick period
     * @param lowPrice the lowest price of the tick period
     * @param closePrice the close price of the tick period
     * @param volume the volume of the tick period
     */
    public BaseTick(ZonedDateTime endTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume) {
        this(endTime, Decimal.valueOf(openPrice), Decimal.valueOf(highPrice), Decimal.valueOf(lowPrice), Decimal
                .valueOf(closePrice), Decimal.valueOf(volume));
    }

    /**
     * Constructor.
     * 
     * @param endTime the end time of the tick period
     * @param openPrice the open price of the tick period
     * @param highPrice the highest price of the tick period
     * @param lowPrice the lowest price of the tick period
     * @param closePrice the close price of the tick period
     * @param volume the volume of the tick period
     */
    public BaseTick(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume) {
        this(endTime, Decimal.valueOf(openPrice), Decimal.valueOf(highPrice), Decimal.valueOf(lowPrice), Decimal
                .valueOf(closePrice), Decimal.valueOf(volume));
    }

    /**
     * Constructor.
     * 
     * @param endTime the end time of the tick period
     * @param openPrice the open price of the tick period
     * @param highPrice the highest price of the tick period
     * @param lowPrice the lowest price of the tick period
     * @param closePrice the close price of the tick period
     * @param volume the volume of the tick period
     */
    public BaseTick(ZonedDateTime endTime, Decimal openPrice, Decimal highPrice, Decimal lowPrice, Decimal closePrice, Decimal volume) {
        this(Duration.ofDays(1), endTime, openPrice, highPrice, lowPrice, closePrice, volume);
    }

    /**
     * Constructor.
     * 
     * @param timePeriod the time period
     * @param endTime the end time of the tick period
     * @param openPrice the open price of the tick period
     * @param highPrice the highest price of the tick period
     * @param lowPrice the lowest price of the tick period
     * @param closePrice the close price of the tick period
     * @param volume the volume of the tick period
     */
    public BaseTick(Duration timePeriod, ZonedDateTime endTime, Decimal openPrice, Decimal highPrice, Decimal lowPrice, Decimal closePrice, Decimal volume) {
        checkTimeArguments(timePeriod, endTime);
        this.timePeriod = timePeriod;
        this.endTime = endTime;
        this.beginTime = endTime.minus(timePeriod);
        this.openPrice = openPrice;
        this.maxPrice = highPrice;
        this.minPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    /**
     * @return the close price of the period
     */
    @Override
    public Decimal getClosePrice() {
        return closePrice;
    }

    /**
     * @return the open price of the period
     */
    @Override
    public Decimal getOpenPrice() {
        return openPrice;
    }

    /**
     * @return the number of trades in the period
     */
    @Override
    public int getTrades() {
        return trades;
    }

    /**
     * @return the max price of the period
     */
    @Override
    public Decimal getMaxPrice() {
        return maxPrice;
    }

    /**
     * @return the whole traded amount of the period
     */
    @Override
    public Decimal getAmount() {
        return amount;
    }

    /**
     * @return the whole traded volume in the period
     */
    @Override
    public Decimal getVolume() {
        return volume;
    }

    /**
     * Adds a trade at the end of tick period.
     * 
     * @param tradeAmount the tradable amount
     * @param tradePrice the price
     */
    @Override
    public void addTrade(double tradeAmount, double tradePrice) {
        addTrade(Decimal.valueOf(tradeAmount), Decimal.valueOf(tradePrice));
    }

    /**
     * Adds a trade at the end of tick period.
     * 
     * @param tradeAmount the tradable amount
     * @param tradePrice the price
     */
    @Override
    public void addTrade(String tradeAmount, String tradePrice) {
        addTrade(Decimal.valueOf(tradeAmount), Decimal.valueOf(tradePrice));
    }

    /**
     * Adds a trade at the end of tick period.
     * 
     * @param tradeAmount the tradable amount
     * @param tradePrice the price
     */
    @Override
    public void addTrade(Decimal tradeAmount, Decimal tradePrice) {
        if (openPrice == null) {
            openPrice = tradePrice;
        }
        closePrice = tradePrice;

        if (maxPrice == null) {
            maxPrice = tradePrice;
        } else {
            maxPrice = maxPrice.isLessThan(tradePrice) ? tradePrice : maxPrice;
        }
        if (minPrice == null) {
            minPrice = tradePrice;
        } else {
            minPrice = minPrice.isGreaterThan(tradePrice) ? tradePrice : minPrice;
        }
        amount = amount.plus(tradeAmount);
        volume = volume.plus(tradeAmount.multipliedBy(tradePrice));
        trades++;
    }

    /**
     * @return the min price of the period
     */
    @Override
    public Decimal getMinPrice() {
        return minPrice;
    }

    /**
     * @return the time period of the tick
     */
    @Override
    public Duration getTimePeriod() {
        return timePeriod;
    }

    /**
     * @return the begin timestamp of the tick period
     */
    @Override
    public ZonedDateTime getBeginTime() {
        return beginTime;
    }

    /**
     * @return the end timestamp of the tick period
     */
    @Override
    public ZonedDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return String.format("{end time: %1s, close price: %2$f, open price: %3$f, min price: %4$f, max price: %5$f, volume: %6$f}", endTime
                .withZoneSameInstant(ZoneId.systemDefault()), closePrice
                        .toDouble(), openPrice.toDouble(), minPrice.toDouble(), maxPrice.toDouble(), volume.toDouble());
    }

    /**
     * @param timestamp a timestamp
     * @return true if the provided timestamp is between the begin time and the end time of the
     *         current period, false otherwise
     */
    @Override
    public boolean inPeriod(ZonedDateTime timestamp) {
        return timestamp != null && !timestamp.isBefore(beginTime) && timestamp.isBefore(endTime);
    }

    /**
     * @return true if this is a bearish tick, false otherwise
     */
    @Override
    public boolean isBearish() {
        return (openPrice != null) && (closePrice != null) && closePrice.isLessThan(openPrice);
    }

    /**
     * @return true if this is a bullish tick, false otherwise
     */
    @Override
    public boolean isBullish() {
        return (openPrice != null) && (closePrice != null) && openPrice.isLessThan(closePrice);
    }

    /**
     * @return a human-friendly string of the end timestamp
     */
    @Override
    public String getDateName() {
        return endTime.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * @return a even more human-friendly string of the end timestamp
     */
    @Override
    public String getSimpleDateName() {
        return endTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * @param timePeriod the time period
     * @param endTime the end time of the tick
     * @throws IllegalArgumentException if one of the arguments is null
     */
    private void checkTimeArguments(Duration timePeriod, ZonedDateTime endTime) {
        if (timePeriod == null) {
            throw new IllegalArgumentException("Time period cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
    }
}
