/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import kiss.Variable;
import trademate.CommonText;
import viewtify.ui.UIColorPicker;
import viewtify.ui.UIComboBox;
import viewtify.ui.ViewDSL;
import viewtify.ui.view.AppearanceSettingView;
import viewtify.ui.view.PreferenceViewBase;

public class AppearanceSetting extends PreferenceViewBase {

    AppearanceSettingView appearance;

    UIComboBox<trademate.ChartTheme> themes;

    UIColorPicker buy;

    UIColorPicker sell;

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable<String> title() {
        return en("Appearance and Language");
    }

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, () -> {
                title(en("General"));
                $(appearance);

                title(en("Related to chart"));
                form(en("Theme"), themes);
                form(CommonText.Buy, buy);
                form(CommonText.Sell, sell);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        themes.initialize(trademate.ChartTheme.builtins()).observe(trademate.ChartTheme::apply);
        buy.sync(trademate.ChartTheme.$.buy);
        sell.sync(trademate.ChartTheme.$.sell);
    }
}