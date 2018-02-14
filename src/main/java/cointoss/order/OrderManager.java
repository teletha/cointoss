/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.Market;

/**
 * @version 2018/02/14 13:59:20
 */
public class OrderManager {

    /** The associated market. */
    private final Market market;

    /** The active orders. */
    private final List<Order> actives = new CopyOnWriteArrayList();

    /**
     * @param market
     */
    public OrderManager(Market market) {
        this.market = market;
    }
}
