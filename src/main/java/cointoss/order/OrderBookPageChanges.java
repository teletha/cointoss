/*
 * Copyright (C) 2021 cointoss Development Team
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

public class OrderBookPageChanges {

    /**
     * The specific API does not tell you the information that the quantity has reached zero, so you
     * should erase any existing data that is within the range of the retrieved data.
     */
    public boolean clearInside = false;

    /** The list of long orders. */
    public List<OrderBookPage> bids = new ArrayList();

    /** The list of short orders. */
    public List<OrderBookPage> asks = new ArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderBookChange [bids=" + bids + ", asks=" + asks + "]";
    }
}