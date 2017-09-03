/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

/**
 * @version 2017/07/30 20:56:12
 */
public enum BitFlyerType {
    BTC_JPY, FX_BTC_JPY, ETC_BTC, BCH_BTC;

    /**
     * <p>
     * Compute full name.
     * </p>
     * 
     * @return
     */
    public String fullName() {
        return "BitFlyer " + name();
    }
}
