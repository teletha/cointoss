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

import java.util.ArrayList;
import java.util.List;

import cointoss.util.Num;

/**
 * @version 2017/11/13 22:52:44
 */
public class OrderBookChange {

    public Num mid_price;

    public List<OrderUnit> bids = new ArrayList();

    public List<OrderUnit> asks = new ArrayList();
}
