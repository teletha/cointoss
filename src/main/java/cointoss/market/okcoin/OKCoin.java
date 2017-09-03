/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.okcoin;

import java.net.URL;

import kiss.I;

/**
 * @version 2017/06/25 12:21:43
 */
public class OKCoin {

    public static void main(String[] args) {
        I.signal("https://www.okcoin.com/api/v1/trades.do?symbol=btc_usd&since=10000")
                .map(URL::new)
                .map(I::json)
                .flatIterable(json -> json.to(OKCoinExecutions.class))
                .to(e -> {
                    System.out.println(e);
                });
    }
}
