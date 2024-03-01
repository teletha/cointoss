/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.setting;

import viewtify.keys.KeyBindingSettingView;
import viewtify.preference.PreferenceView;
import viewtify.update.UpdateSettingView;

public class SettingView extends PreferenceView {

    public SettingView() {
        manage(AppearanceSetting.class, PerformanceSettingView.class, KeyBindingSettingView.class, NotificatorSetting.class, BitFlyerSetting.class, UpdateSettingView.class);
    }
}
