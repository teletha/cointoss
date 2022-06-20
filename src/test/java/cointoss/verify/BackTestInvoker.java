/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.verify;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.trade.bot.RandomWalker;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest.with.service(BitFlyer.FX_BTC_JPY)
                .startRandom()
                .endDuration(365)
                .traders(new RandomWalker())
                .fast()
                .detail(false)
                .initialBaseCurrency(3000000)
                .run();

        // ZonedDateTime start = Chrono.utc(2022, 1, 10);
        // ZonedDateTime end = Chrono.utc(2022, 1, 11);
        // SignalSynchronizer synchronizer = new SignalSynchronizer();
        //
        // Binance.FUTURE_EHT_USDT.openInterest().query(start.toEpochSecond(),
        // end.toEpochSecond()).plug(synchronizer.sync()).to(oi -> {
        // System.out.println(oi);
        // });
        //
        // new ExecutionLog(Binance.FUTURE_EHT_USDT).at(start).plug(synchronizer.sync()).to(exe -> {
        // System.out.println(exe);
        // });
    }
}