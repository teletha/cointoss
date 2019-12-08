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

import static trademate.setting.SettingStyles.Root;

import kiss.Managed;
import kiss.Singleton;
import viewtify.ui.UI;
import viewtify.ui.View;

/**
 * @version 2018/09/24 7:11:35
 */
@Managed(value = Singleton.class)
public class ChartSetting extends View {

    class view extends UI {
        {
            $(vbox, Root, () -> {

            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }
}
