/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import kiss.Variable;
import viewtify.preference.Preferences;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class PerformanceSettingView extends View {

    UIComboBox<Integer> refreshRate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable<String> title() {
        return en("Performance");
    }

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, () -> {
                form(en("UI refresh rate (FPS)"), refreshRate);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        PerformanceSetting setting = Preferences.of(PerformanceSetting.class);

        refreshRate.items(1, 2, 5, 10, 16, 25, 33, 60)
                .sync(setting.refreshRate, x -> Math.round(1000_000_000 / x), v -> (long) Math.round(1000_000_000 / v));
    }
}