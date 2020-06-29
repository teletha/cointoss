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

import java.util.List;

import kiss.Managed;
import kiss.Singleton;
import transcript.Transcript;
import viewtify.ui.UIComboBox;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

@Managed(value = Singleton.class)
public class GeneralSetting extends View {

    UIComboBox<String> language;

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
        language.initialize(List.of("en", "ja")).observe(Transcript.lang::set);
    }
}
