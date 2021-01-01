/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

public enum MarketType {
    SPOT, DERIVATIVE;

    /**
     * Type detection helper.
     * 
     * @return
     */
    public boolean isSpot() {
        return this == SPOT;
    }

    /**
     * Type detection helper.
     * 
     * @return
     */
    public boolean isDerivative() {
        return this == DERIVATIVE;
    }
}