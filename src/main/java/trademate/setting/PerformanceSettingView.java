/*
 * Copyright (C) 2025 The COINTOSS Development Team
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
import viewtify.style.FormStyles;
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
                form(en("Drawing frame rate"), FormStyles.Column4, refreshRate);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        PerformanceSetting setting = Preferences.of(PerformanceSetting.class);

        refreshRate.items(1, 2, 3, 4, 5, 10, 16, 24, 30, 40, 50, 60, 120)
                .render(v -> v + " fps")
                .sync(setting.refreshRate, x -> Math.round(1000 / x), v -> (long) Math.round(1000 / v));
    }
}