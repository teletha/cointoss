/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import kiss.Manageable;
import kiss.Singleton;
import viewtify.View;

/**
 * @version 2018/08/27 18:53:30
 */
@Manageable(lifestyle = Singleton.class)
public class BitFlyerSetting extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        System.out.println("Market View");
    }
}
