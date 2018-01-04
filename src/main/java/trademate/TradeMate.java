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

import javafx.stage.Stage;
import javafx.stage.Window;

import cointoss.market.bitflyer.BitFlyer;
import viewtify.ActivationPolicy;
import viewtify.UI;
import viewtify.User;
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
                .load(BitFlyer.FX_BTC_JPY.fullName(), tab -> new TradingView(BitFlyer.FX_BTC_JPY, tab))
                .initial(0)
                .when(User.Click, e -> {
                    if (e.getClickCount() == 2) {
                        Window window = root().getScene().getWindow();

                        if (window instanceof Stage) {
                            Stage stage = (Stage) window;

                            if (stage.isMaximized()) {
                                stage.setMaximized(false);
                            } else {
                                stage.setMaximized(true);
                            }
                        }
                    }
                });
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Viewtify.activate(TradeMate.class, ActivationPolicy.Latest);
    }
}
