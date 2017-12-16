/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.chart2;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import cointoss.util.Num;

/**
 * @author RobTerpilowski
 */
public class BarData implements Serializable {

    public static long serialVersionUID = 1L;

    public static final double NULL = -9D;

    public static final int OPEN = 1;

    public static final int HIGH = 2;

    public static final int LOW = 3;

    public static final int CLOSE = 4;

    public enum LENGTH_UNIT {

        TICK, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    };

    protected Num open;

    protected Num high;

    protected Num low;

    protected Num close;

    protected Num volume = Num.ZERO;

    protected Num openInterest = Num.ZERO;

    protected int barLength = 1;

    protected ZonedDateTime dateTime;

    public BarData() {
    }

    public BarData(ZonedDateTime dateTime, Num open, Num high, Num low, Num close, Num volume) {
        this.dateTime = dateTime;
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
    }

    /**
     * Creates a new instance of a Bar
     *
     * @param date The date of this bar.
     * @param open The open price.
     * @param high The high price.
     * @param low The low price.
     * @param close The closing price.
     * @param volume The volume for the bar.
     * @param openInterest The open interest for the bar.
     */
    public BarData(ZonedDateTime dateTime, Num open, Num high, Num low, Num close, Num volume, Num openInterest) {
        this(dateTime, open, high, low, close, volume);
        this.openInterest = openInterest;
    }// constructor()

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the open price of this bar.
     */
    public Num getOpen() {
        return open;
    }

    /**
     * @return the High price of this bar.
     */
    public Num getHigh() {
        return high;
    }

    /*
     * @return the Low price of this Bar.
     */
    public Num getLow() {
        return low;
    }

    /**
     * @return the close price for this bar.
     */
    public Num getClose() {
        return close;
    }

    /**
     * @return the Volume for this bar.
     */
    public Num getVolume() {
        return volume;
    }

    /**
     * @return the open interest for this bar.
     */
    public Num getOpenInterest() {
        return openInterest;
    }

    /**
     * Sets the open price for this bar.
     *
     * @param open The open price for this bar.
     */
    public void setOpen(Num open) {
        this.open = open;
    }

    /**
     * Sets the high price for this bar.
     *
     * @param high The high price for this bar.
     */
    public void setHigh(Num high) {
        this.high = high;
    }

    /**
     * Sets the low price for this bar.
     *
     * @param low The low price for this bar.
     */
    public void setLow(Num low) {
        this.low = low;
    }

    /**
     * Sets the closing price for this bar.
     *
     * @param close The closing price for this bar.
     */
    public void setClose(Num close) {
        this.close = close;
    }

    /**
     * Sets the volume for this bar.
     *
     * @param volume Sets the volume for this bar.
     */
    public void setVolume(Num volume) {
        this.volume = volume;
    }

    /**
     * Updates the last price, adjusting the high and low
     * 
     * @param close The last price
     */
    public void update(Num close) {
        if (close.isGreaterThan(high)) {
            high = close;
        }

        if (close.isLessThan(low)) {
            low = close;
        }
        this.close = close;
    }

    /**
     * Sets the open interest for this bar.
     *
     * @param openInterest The open interest for this bar.
     */
    public void setOpenInterest(Num openInterest) {
        this.openInterest = openInterest;
    }

    protected BigDecimal format(double price) {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(dateTime);
        sb.append(" Open: ").append(open);
        sb.append(" High: ").append(high);
        sb.append(" Low: ").append(low);
        sb.append(" Close: ").append(close);
        sb.append(" Volume: ").append(volume);
        sb.append(" Open Int ").append(openInterest);

        return sb.toString();
    }// toString()

}