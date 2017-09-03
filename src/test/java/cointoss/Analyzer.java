/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import cointoss.Amount;
import cointoss.TimeSeries;
import cointoss.indicator.trackers.MACDIndicator;
import cointoss.market.bitflyer.BitFlyerBTCFXBuilder;
import kiss.I;

/**
 * @version 2017/09/03 11:33:58
 */
public class Analyzer {

    public static void main(String[] args) {
        I.load(Amount.Codec.class, false);

        TimeSeries ticks = TimeSeries.load(BitFlyerBTCFXBuilder.class);

        MACDIndicator macd = ticks.macd();

        for (int i = 0; i < ticks.size(); i++) {
            System.out.println(macd.getValue(i));
        }
    }
}
