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

import static trademate.setting.SettingStyles.*;

import kiss.Manageable;
import kiss.Singleton;
import viewtify.ui.UI;
import viewtify.ui.View;

/**
 * @version 2018/09/06 23:46:18
 */
@Manageable(lifestyle = Singleton.class)
public class GeneralSetting extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, Root, () -> {

                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }
}
