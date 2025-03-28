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

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import kiss.Signal;
import viewtify.preference.Preferences;

public class PerformanceSetting extends Preferences {

    public final LongPreference refreshRate = initialize(2L).requireMin(1).requireMax(1000);

    /**
     * Helper method to apply refresh rate.
     * 
     * @return
     */
    public static <X> Function<Signal<X>, Signal<X>> applyUIRefreshRate() {
        return signal -> signal.throttle(Preferences.of(PerformanceSetting.class).refreshRate, TimeUnit.MILLISECONDS);
    }
}