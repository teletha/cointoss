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

import cointoss.execution.LogType;
import cointoss.market.ftx.FTX;
import cointoss.trading.LiquidationEater;

public class BackTestInvoker {

    public static void main(String[] args) throws InterruptedException {
        BackTest.with.service(FTX.BTC_PERP)
                .start(2021, 10, 1)
                .end(2022, 1, 30)
                .traders(new LiquidationEater())
                .initialBaseCurrency(3000000)
                .type(LogType.Normal)
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