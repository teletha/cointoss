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

import kiss.Variable;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.model.PreferenceModel;

public class TradeMateSetting extends PreferenceModel<TradeMateSetting> {

    public final Variable<Theme> theme = initialize(Theme.Dark).syncTo(Viewtify::manage);

    /**
     * Hide constructor.
     */
    private TradeMateSetting() {
        restore().auto();
    }
}
