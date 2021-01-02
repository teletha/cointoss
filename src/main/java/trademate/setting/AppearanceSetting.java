/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import java.util.Locale;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.CommonText;
import trademate.Theme;
import viewtify.ui.UIColorPicker;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIFontPicker;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class AppearanceSetting extends View {

    UIComboBox<Locale> language;

    UIFontPicker font;

    UIComboBox<Theme> themes;

    UIColorPicker buy;

    UIColorPicker sell;

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, () -> {
                $(vbox, Block, () -> {
                    label(en("General"), Heading);
                    form(en("Language"), language);
                    form(en("Font"), font);
                });
                $(vbox, Block, () -> {
                    label(en("Color Combination"), Heading);
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
        language.items(Locale.ENGLISH, Locale.JAPANESE, Locale.CHINESE)
                .render(lang -> lang.getDisplayLanguage(lang))
                .renderSelected(lang -> lang.getDisplayLanguage(lang))
                .select(Locale.forLanguageTag(I.Lang.exact()))
                .observing(lang -> I.Lang.set(lang.getLanguage()));

        themes.initialize(Theme.builtins()).observe(Theme::apply);
        buy.sync(Theme.$.buy);
        sell.sync(Theme.$.sell);
    }
}