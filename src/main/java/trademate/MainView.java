/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import javafx.fxml.FXML;

import cointoss.market.bitflyer.BitFlyer;
import viewtify.View;
import viewtify.ui.UITabPane;

/**
 * @version 2017/11/29 8:18:42
 */
public class MainView extends View {

    private @FXML UITabPane main;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        main.load(BitFlyer.FX_BTC_JPY.fullName(), () -> new TradingView(BitFlyer.FX_BTC_JPY)).initial(0);
    }
}
