/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.log;

import java.nio.file.Path;
import java.nio.file.Paths;

import cointoss.MarketLog;
import cointoss.market.bitflyer.BitFlyer;
import filer.Filer;

/**
 * @version 2018/04/20 19:04:42
 */
public class Log {

    public static void main(String[] args) {
        Path path = Paths.get("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY");
        MarketLog log = BitFlyer.FX_BTC_JPY.log();

        Filer.walk(path, "execution20171209.log").to(file -> {
            log.compact(file);
            System.out.println(file);
        });
    }

}
