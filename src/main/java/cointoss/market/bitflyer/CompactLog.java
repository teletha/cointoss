/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cointoss.Execution;
import cointoss.util.Num;
import filer.Filer;

/**
 * @version 2018/04/10 8:40:27
 */
public class CompactLog {

    public static void main(String[] args) throws IOException {

        List<String> lines = new ArrayList();
        AtomicReference<Execution> prev = new AtomicReference();

        Filer.read("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404.log").map(Execution::new).to(exe -> {
            Execution previous = prev.getAndSet(exe);

            if (previous == null) {
                lines.add(exe.toString());
            } else {
                long id = exe.id;
                String buy_child_order_acceptance_id = exe.buy_child_order_acceptance_id;
                String sell_child_order_acceptance_id = exe.sell_child_order_acceptance_id;
                Num price = exe.price;
                Num size = exe.size;
                ZonedDateTime exec_date = exe.exec_date;

                lines.add(id + " " + exec_date.toLocalDateTime() + " " + exe.side
                        .mark() + " " + price + " " + size + " " + buy_child_order_acceptance_id + " " + sell_child_order_acceptance_id);
            }
        });

        Files.write(Paths.get("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404COMAPT.log"), lines);
    }
}
