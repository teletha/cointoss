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

import cointoss.MarketLog;
import cointoss.util.Span;
import cointoss.MarketBackend;

/**
 * @version 2017/09/08 18:23:03
 */
public enum BitFlyer {
    BTC_JPY, FX_BTC_JPY, ETC_BTC, BCH_BTC;

    /** Sample trend */
    public static final Span SampleTrend = new Span(2017, 5, 29, 2017, 6, 5);

    /** Sample of range trend */
    public static final Span RangeTrend = new Span(2017, 5, 29, 2017, 7, 29);

    /** Sample of up trend */
    public static final Span UpTrend = new Span(2017, 7, 16, 2017, 8, 29);

    /** Sample of down trend */
    public static final Span DownTrend = new Span(2017, 6, 11, 2017, 7, 16);

    /**
     * <p>
     * Compute full name.
     * </p>
     * 
     * @return
     */
    public String fullName() {
        return "BitFlyer " + name();
    }

    /**
     * Create {@link MarketLog} for this market type.
     * 
     * @return
     */
    public MarketLog log() {
        return new BitFlyerLog(this);
    }

    /**
     * Create {@link MerketService} for this market type.
     * 
     * @return
     */
    public MarketBackend service() {
        return new BitFlyerBackend(this);
    }
}
