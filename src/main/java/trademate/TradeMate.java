/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import cointoss.market.bitflyer.BitFlyerService;
import viewtify.ActivationPolicy;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UITabPane;

/**
 * @version 2017/12/04 13:07:34
 */
public class TradeMate extends View {

    private @UI UITabPane main;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        main.load("Setting", SettingView.class)
                .load("Back Test", BackTestView.class)
                .load("BitFlyer FX", tab -> new TradingView(BitFlyerService.BTC_JPY, tab))
                .initial(0);
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        Viewtify.activate(TradeMate.class, ActivationPolicy.Latest);
    }
}
