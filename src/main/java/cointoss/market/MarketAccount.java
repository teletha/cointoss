/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;

/**
 * @version 2018/09/06 21:46:36
 */
@Managed(value = Singleton.class)
public abstract class MarketAccount<Self extends MarketAccount> implements Storable<Self> {

    /**
     * Validate this account info.
     * 
     * @return
     */
    protected abstract boolean validate();
}