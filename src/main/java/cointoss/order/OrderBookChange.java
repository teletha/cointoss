/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 2018/04/15 20:10:04
 */
public class OrderBookChange {

    /** The list of long orders. */
    public List<OrderUnit> bids = new ArrayList();

    /** The list of short orders. */
    public List<OrderUnit> asks = new ArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderBookChange [bids=" + bids + ", asks=" + asks + "]";
    }
}
