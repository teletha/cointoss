/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

public enum OrderType {
    Maker, Taker;

    /**
     * Detect order type.
     * 
     * @return A result.
     */
    public boolean isTaker() {
        return this == Taker;
    }

    /**
     * Detect order type.
     * 
     * @return A result.
     */
    public boolean isMaker() {
        return this == Maker;
    }
}