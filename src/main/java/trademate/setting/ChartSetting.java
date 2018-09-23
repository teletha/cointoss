/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.setting;

import static trademate.setting.SettingStyles.*;

import kiss.Manageable;
import kiss.Singleton;
import viewtify.ui.UI;
import viewtify.ui.View;

/**
 * @version 2018/09/24 7:11:35
 */
@Manageable(lifestyle = Singleton.class)
public class ChartSetting extends View {

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
