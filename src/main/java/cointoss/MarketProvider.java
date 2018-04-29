/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

/**
 * @version 2018/04/25 4:06:43
 */
public interface MarketProvider {

    /**
     * Provide market backend.
     * 
     * @return
     */
    MarketService service();

    /**
     * Provide market log.
     * 
     * @return
     */
    MarketLog log();

    /**
     * Compute market name.
     * 
     * @return
     */
    String name();

    /**
     * Compute exchange name.
     * 
     * @return
     */
    default String exchangeName() {
        return getClass().getSimpleName();
    }

    /**
     * Full market name.
     * 
     * @return
     */
    default String fullName() {
        return exchangeName() + " " + name();
    }
}
