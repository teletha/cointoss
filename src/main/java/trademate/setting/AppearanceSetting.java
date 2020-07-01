/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

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

    interface style extends SettingStyles {
    }

    class view extends ViewDSL {
        {
            $(vbox, () -> {
                $(vbox, style.Block, () -> {
                    label(en("Colors"), style.Heading);
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