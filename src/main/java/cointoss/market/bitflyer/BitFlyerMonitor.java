/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.Market;
import cointoss.Trading;

/**
 * @version 2017/09/08 18:40:12
 */
public class BitFlyerMonitor extends Trading {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.observeExecutionBySize(11).to(exe -> {
            System.out.println("大口 " + exe.exec_date.withNano(0).toLocalDateTime().plusHours(9) + "  " + exe.side
                    .mark() + exe.cumulativeSize + " @" + exe.price);
        });

        market.minute1.to(tick -> {
            System.out.println(tick.start + "  1min " + market.minute1.trend() + "   15min " + market.minute15
                    .trend() + "    30min " + market.minute30
                            .trend() + "   1hour " + market.hour1.trend() + "   2hour " + market.hour2.trend());
        });
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Market market = new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(3), new BitFlyerMonitor());
    }
}
