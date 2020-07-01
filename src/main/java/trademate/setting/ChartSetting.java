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

import kiss.Managed;
import kiss.Singleton;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class ChartSetting extends View {

    interface style extends SettingStyles {
    }

    class view extends ViewDSL {
        {
            $(vbox, () -> {
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