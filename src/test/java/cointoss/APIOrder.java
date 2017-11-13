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

import cointoss.market.bitflyer.BitFlyer;

/**
 * @version 2017/11/13 13:02:30
 */
public class APIOrder {

    public static void main(String[] args) {
        MarketBackend service = BitFlyer.FX_BTC_JPY.service();
        service.request(Order.limitLong(0.01, 500000)).to(id -> {
            System.out.println("SUCCESS " + id);
        }, e -> {
            e.printStackTrace();
        });
    }
}
