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

import cointoss.market.bitflyer.BitFlyer;
import kiss.I;
import trademate.setting.SettingView;
import viewtify.ActivationPolicy;
import viewtify.Viewtify;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UITabPane;
import viewtify.ui.View;

/**
 * @version 2018/09/06 23:45:59
 */
public class TradeMate extends View {

    static {
        I.load(TradeMate.class, false);
    }

    private UITabPane main;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                $(main);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        main.load("Setting", SettingView.class)
                .load("Back Test", BackTestView.class)
                .load("BitFlyer FX", tab -> new TradingView(BitFlyer.FX_BTC_JPY))
                .load("BitFlyer BTC", tab -> new TradingView(BitFlyer.BTC_JPY))
                .initial(0);
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // configure logging property
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        Viewtify.activate(TradeMate.class, ActivationPolicy.Latest);
    }
}
