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

import cointoss.Market;
import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.verify.BackTestView;
import transcript.Lang;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeTester extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // initialize logger for non-main thread
        Logger log = LogManager.getLogger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));

        // activate application
        Viewtify.application()
                .use(Theme.Dark)
                .icon("icon/bot.png")
                .language(Lang.of(I.env("language", Locale.getDefault().getLanguage())))
                .onTerminating(Network::terminate)
                .activate(TradeTester.class);
    }
}
