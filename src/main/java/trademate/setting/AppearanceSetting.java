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

import trademate.CommonText;
import viewtify.ui.UIColorPicker;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.view.AppearanceSettingView;

public class AppearanceSetting extends View {

    AppearanceSettingView appearance;

    UIComboBox<trademate.ChartTheme> themes;

    UIColorPicker buy;

    UIColorPicker sell;

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, () -> {
                $(vbox, Block, () -> {
                    label(en("General"), Heading);
                    $(appearance);
                });
                $(vbox, Block, () -> {
                    label(en("Chart Color"), Heading);
                    form(en("Theme"), themes);
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
        themes.initialize(trademate.ChartTheme.builtins()).observe(trademate.ChartTheme::apply);
        buy.sync(trademate.ChartTheme.$.buy);
        sell.sync(trademate.ChartTheme.$.sell);
    }
}