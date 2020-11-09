/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import cointoss.Direction;
import trademate.Theme;
import viewtify.Command;
import viewtify.Key;
import viewtify.style.FormStyles;
import viewtify.ui.UIButton;
import viewtify.ui.UILabel;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;

public class OrderView extends View {

    UILabel total;

    UIButton buy;

    UIButton sell;

    UIButton clear;

    class view extends ViewDSL {
        {
            $(hbox, FormStyles.FormRow, () -> {
                $(buy, FormStyles.FormInputMin);
                $(clear, FormStyles.FormInputMin);
                $(sell, FormStyles.FormInputMin);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Commands.TakeBuy.shortcut(Key.Q).contribute(this::buy);
        Commands.Clear.shortcut(Key.W).contribute(this::clear);
        Commands.TakeSell.shortcut(Key.E).contribute(this::sell);

        buy.text(en("Buy")).color(Theme.colorBy(Direction.BUY)).when(User.Action, Commands.TakeBuy);
        clear.text(en("Clearing")).when(User.Action, Commands.Clear);
        sell.text(en("Sell")).color(Theme.colorBy(Direction.SELL)).when(User.Action, Commands.TakeSell);
    }

    private void buy() {

        System.out.println("Buy");
    }

    private void sell() {
        System.out.println("Sell");

    }

    private void clear() {
        System.out.println("Clear");
    }

    /**
     * 
     */
    private enum Commands implements Command<Commands> {
        TakeBuy, Clear, TakeSell;
    }
}
