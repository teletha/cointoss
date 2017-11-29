/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import viewtify.ActivationPolicy;
import viewtify.Viewtify;

/**
 * @version 2017/11/13 16:58:58
 */
public class TradeMate {

    /** Cache for markets. */
    private final Map<Object, Market> markets = new ConcurrentHashMap();

    /**
     * Select the trading market.
     * 
     * @param bitFlyer
     * @return
     */
    public final Market tradeAt(BitFlyer bitFlyer) {
        return markets.computeIfAbsent(bitFlyer, key -> {
            return new Market(bitFlyer.service(), bitFlyer.log().fromToday());
        });
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Viewtify.activate(MainView.class, ActivationPolicy.Latest);
    }
}
