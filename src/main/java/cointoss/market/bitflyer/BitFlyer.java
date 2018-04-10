/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import cointoss.MarketBackend;
import cointoss.MarketLog;
import cointoss.MarketProvider;
import cointoss.util.Span;

/**
 * @version 2018/04/10 14:18:05
 */
public enum BitFlyer implements MarketProvider {
    BTC_JPY, FX_BTC_JPY, ETC_BTC, BCH_BTC;

    /** Sample trend */
    public static final Span SampleTrend = new Span(2017, 5, 29, 2017, 6, 5);

    /** Sample of range trend */
    public static final Span RangeTrend = new Span(2017, 5, 29, 2017, 7, 29);

    /** Sample of up trend */
    public static final Span UpTrend = new Span(2017, 7, 16, 2017, 8, 29);

    /** Sample of down trend */
    public static final Span DownTrend = new Span(2017, 6, 11, 2017, 7, 16);

    /** cache */
    private final MarketLog marketLog = new BitFlyerLog(this);

    /** cache */
    private final MarketBackend marketBackend = new BitFlyerBackend(this);

    /**
     * {@inheritDoc}
     */
    @Override
    public String orgnizationName() {
        return getClass().getSimpleName();
    }

    /**
     * <p>
     * Compute full name.
     * </p>
     * 
     * @return
     */
    public String marketName() {
        return orgnizationName() + " " + name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketLog log() {
        return marketLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MarketBackend service() {
        return marketBackend;
    }
}
