/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import static trademate.CommonText.*;
import static trademate.setting.SettingStyles.*;
import static transcript.Transcript.en;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.Theme;
import viewtify.ui.UI;
import viewtify.ui.UIColorPicker;
import viewtify.ui.View;

@Managed(value = Singleton.class)
public class AppearanceSetting extends View {

    private Theme theme = I.make(Theme.class);

    private UIColorPicker buy;

    private UIColorPicker sell;

    class view extends UI {
        {
            $(vbox, Root, () -> {
                $(vbox, Block, () -> {
                    label(en("Colors"), Heading);
                    $(hbox, FormRow, () -> {
                        label(Buy, FormLabel);
                        $(buy, FormInput);
                    });
                    $(hbox, FormRow, () -> {
                        label(Sell, FormLabel);
                        $(sell, FormInput);
                    });
                });
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        buy.sync(theme.Long);
        sell.sync(theme.Short);
    }
}
