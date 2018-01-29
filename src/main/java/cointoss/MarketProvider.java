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
 * @version 2018/01/29 20:59:07
 */
public interface MarketProvider {

    /**
     * Provide market backend.
     * 
     * @return
     */
    MarketBackend service();

    /**
     * Provide market log.
     * 
     * @return
     */
    MarketLog log();
}
