/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import cointoss.util.Network;
import kiss.I;

public class SignalTest {

    public static void main(String[] args) throws InterruptedException {
        BitFlyerAccount account = I.make(BitFlyerAccount.class);

        Network network = new Network();
        network.signalr("https://signal.bitflyer.com/signalr/hubs", "account_id=" + account.accountId + "&token=" + account.accountToken + "&products=FX_BTC_JPY%2Cheartbeat", "BFEXHub", "ReceiveOrderUpdates")
                .to(e -> {
                    System.out.println(e);
                });

        Thread.sleep(10000);
    }
}
