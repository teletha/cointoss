/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Network;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import trademate.setting.SettingView;
import transcript.Lang;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UITabPane;
import viewtify.ui.View;

/**
 * @version 2018/09/16 8:43:30
 */
@Manageable(lifestyle = Singleton.class)
public class TradeMate extends View {

    private UITabPane main;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
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
                .initial(0);
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // initialize logger for non-main thread
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        // activate application
        Viewtify.application()
                .use(Theme.Dark)
                .icon("icon/app.png")
                .language(Lang.of(I.env("language", Locale.getDefault().getLanguage())))
                .onTerminating(Network::terminate)
                .activate(TradeMate.class);
    }
}
