/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

/**
 * @version 2017/07/24 14:17:46
 */
public enum OrderType {
    LIMIT, MARKET;

    /**
     * Helper.
     * 
     * @return
     */
    public boolean isMarket() {
        return this == MARKET;
    }
}
