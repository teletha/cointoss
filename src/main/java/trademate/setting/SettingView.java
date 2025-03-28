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

import viewtify.keys.KeyBindingSettingView;
import viewtify.preference.PreferenceView;
import viewtify.update.UpdateSettingView;

public class SettingView extends PreferenceView {

    public SettingView() {
        manage(AppearanceSetting.class, PerformanceSettingView.class, KeyBindingSettingView.class, NotificatorSetting.class, BitFlyerSetting.class, UpdateSettingView.class);
    }
}