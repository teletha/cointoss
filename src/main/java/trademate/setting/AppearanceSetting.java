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

import static transcript.Transcript.*;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.CommonText;
import trademate.Theme;
import viewtify.ui.UIColorPicker;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class AppearanceSetting extends View {

    private Theme theme = I.make(Theme.class);

    private UIColorPicker buy;

    private UIColorPicker sell;

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, Root, () -> {
                $(vbox, Block, () -> {
                    label(en("Colors"), Heading);
                    form(CommonText.Buy, buy);
                    form(CommonText.Sell, sell);
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
