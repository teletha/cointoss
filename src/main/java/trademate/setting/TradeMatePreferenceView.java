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
import viewtify.ui.dock.DockSystem;
import viewtify.ui.dock.Dockable;
import viewtify.update.UpdateSettingView;

public class TradeMatePreferenceView extends PreferenceView implements Dockable {

    public TradeMatePreferenceView() {
        manage(AppearanceSetting.class, KeyBindingSettingView.class, NotificatorSetting.class, BitFlyerSetting.class, UpdateSettingView.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDock() {
        DockSystem.register(id()).text(title()).contentsLazy(tab -> this);
    }
}
