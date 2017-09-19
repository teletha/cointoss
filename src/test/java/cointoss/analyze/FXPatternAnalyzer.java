/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import cointoss.BackTester;
import cointoss.market.bitflyer.BitFlyer;

/**
 * @version 2017/09/20 2:36:26
 */
public class FXPatternAnalyzer {

    public static void main(String[] args) {
        BackTester.with().baseCurrency(1000000).log(BitFlyer.FX_BTC_JPY.log().rangeAll()).trial(1).run();
    }
}
