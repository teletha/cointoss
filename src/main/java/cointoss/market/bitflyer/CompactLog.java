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
        AtomicReference<Execution> ref = new AtomicReference();

        Filer.read("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404.log").map(Execution::new).to(exe -> {
            Execution previous = ref.getAndSet(exe);

            if (previous == null) {
                lines.add(exe.toString());
            } else {
                long id = exe.id - previous.id;

                String buy_child_order_acceptance_id = compact(exe.buy_child_order_acceptance_id, previous.buy_child_order_acceptance_id);
                String sell_child_order_acceptance_id = compact(exe.sell_child_order_acceptance_id, previous.sell_child_order_acceptance_id);
                Num price = exe.price.minus(previous.price);
                String size = exe.size.equals(previous.size) ? "" : exe.size.toString();
                long now = exe.exec_date.toInstant().toEpochMilli();
                long prevTime = previous.exec_date.toInstant().toEpochMilli();
                long diffTime = now - prevTime;

                lines.add((id == 1 ? "" : id) + " " + (diffTime == 0 ? "" : diffTime) + " " + (exe.side == previous.side ? ""
                        : exe.side.mark()) + " " + (price.isZero() ? ""
                                : price) + " " + size + " " + buy_child_order_acceptance_id + " " + sell_child_order_acceptance_id);
            }
        });

        Files.write(Paths.get("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404COMAPT.log"), lines);
    }

    private static String compact(String now, String prev) {
        int min = Math.min(now.length(), prev.length());

        for (int i = 0; i < min; i++) {
            char charNow = now.charAt(i);
            char charPrev = prev.charAt(i);

            if (charNow != charPrev) {
                min = i;
                break;
            }
        }

        if (min == now.length()) {
            // same
            return "";
        } else if (min == 0) {
            return now;
        } else {
            return at(min) + now.substring(min);
        }
    }

    private static char at(int index) {
        int firstIndexAlpha = 'A';
        return (char) (firstIndexAlpha + index);
    }
}
