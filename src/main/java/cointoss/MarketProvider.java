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
 * @version 2018/04/29 17:46:48
 */
public interface MarketProvider {

    /**
     * Provide market backend service.
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

    /**
     * Compute base {@link Currency}.
     * 
     * @return
     */
    default Currency base() {
        return Currency.getInstance(name().substring(name().indexOf("_") + 1));
    }

    /**
     * Compute target {@link Currency}.
     * 
     * @return
     */
    default Currency target() {
        return Currency.getInstance(name().substring(0, name().indexOf("_")));
    }
}
