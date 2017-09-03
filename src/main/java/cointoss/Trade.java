/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

/**
 * @version 2017/08/24 19:42:21
 */
public abstract class Trade {

    public abstract void initialize(Market market);

    /**
     * Invoke whenever this trade has no position.
     * 
     * @param exe
     */
    public abstract void onNoPosition(Market market, Execution exe);

    /**
     * Clear the current position
     * 
     * @param seconds
     */
    protected void clearPositionAfter(long seconds) {

    }
}
