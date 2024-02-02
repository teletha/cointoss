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

import cointoss.Market;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.dock.TradeMateDockRegister;
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
        TradeMateDockRegister docks = I.make(TradeMateDockRegister.class);

        docks.registerLayout();
        docks.registerMenu();
    }

    /**
     * Entry point.
     * 
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        I.load(Market.class);

        // activate application
        Viewtify.application() //
                .error((msg, error) -> {
                    I.error(msg);
                    I.error(error);
                    error.printStackTrace();
                })
                .icon("icon/tester.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeTester.class);
    }
}