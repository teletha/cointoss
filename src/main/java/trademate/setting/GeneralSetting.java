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

import java.util.List;
import java.util.Locale;

import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class GeneralSetting extends View {

    UIComboBox<Locale> language;

    class view extends ViewDSL implements SettingStyles {
        {
            $(vbox, Block, () -> {
                label(en("General"), Heading);
                form(en("Language"), language);
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        language.initialize(Locale.forLanguageTag(I.Lang.v), List.of(Locale.ENGLISH, Locale.JAPANESE))
                .render(lang -> lang.getDisplayLanguage(Locale.getDefault()))
                .renderSelected(lang -> lang.getDisplayLanguage(Locale.getDefault()))
                .observing(lang -> I.Lang.set(lang.getLanguage()));
    }
}