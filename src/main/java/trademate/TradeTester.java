/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.SynchronousMode;

import cointoss.Market;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import stylist.Style;
import stylist.StyleDSL;
import typewriter.sqlite.SQLite;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeTester extends View {

    interface style extends StyleDSL {
        Style root = () -> {
            display.width(1100, px).height(700, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI, style.root);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.initialize();
    }

    /**
     * Entry point.
     * 
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        I.load(Market.class);
        I.env("typewriter.sqlite", "jdbc:sqlite:.log/market.sqlite");
        SQLite.configure(config -> {
            config.setJournalMode(JournalMode.WAL);
            config.setSynchronous(SynchronousMode.OFF);
        });

        // activate application
        Viewtify.application().icon("icon/tester.png").onTerminating(EfficientWebSocket::shutdownNow).activate(TradeTester.class);
    }
}