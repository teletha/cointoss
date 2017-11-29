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

import cointoss.market.bitflyer.BitFlyer;
import viewtify.Viewty;

/**
 * @version 2017/11/29 10:50:06
 */
public class TradingView extends Viewty {

    // private @FXML ExecutionView executionView;

    /**
     * 
     */
    public TradingView(BitFlyer market) {
        System.out.println(market);

        System.out.println(root().lookup(".SELL"));
    }
}
