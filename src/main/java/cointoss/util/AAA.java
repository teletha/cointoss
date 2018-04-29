/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.JsonObject;

import cointoss.Execution;
import cointoss.Side;

/**
 * @version 2018/04/29 6:51:02
 */
public class AAA {

    public static void main(String[] args) throws Exception {
        Path file = Paths.get(".log/bitflyer/key.txt");
        List<String> lines = Files.readAllLines(file);
        String id = lines.get(4);
        String token = lines.get(5);

        String uri = "https://lightning.bitflyer.jp";

        Network.signalr(uri, "account_id=" + id + "&token=" + token + "&products=FX_BTC_JPY,heartbeat", "BFEXHub", "ReceiveOrderUpdates")
                .to(e -> {
                    JsonObject root = e.getAsJsonArray().get(0).getAsJsonObject();
                    JsonObject order = root.getAsJsonObject("order");
                    JsonObject event = root.getAsJsonArray("order_event_updates").get(0).getAsJsonObject();

                    String orderId = order.getAsJsonPrimitive("order_id").getAsString();
                    String acceptId = order.getAsJsonPrimitive("order_ref_id").getAsString();
                    String type = order.getAsJsonPrimitive("OrderStateForGUI").getAsString();

                    if (type.equals("COMPLETED") || type.equals("PARTIAL")) {
                        Num sfd = event.has("sfd") ? Num.of(event.getAsJsonPrimitive("sfd").getAsDouble()) : Num.ZERO;

                        Execution exe = new Execution();
                        exe.side = Side.parse(event.getAsJsonPrimitive("side").getAsString());
                        exe.size = Num.of(event.getAsJsonPrimitive("size").getAsDouble());
                        exe.price = Num.of(event.getAsJsonPrimitive("price").getAsDouble());
                    } else if (type.equals("ACTIVE")) {

                    } else if (type.equals("CANCELED")) {

                    }

                    System.out.println(root);
                });
    }
}
