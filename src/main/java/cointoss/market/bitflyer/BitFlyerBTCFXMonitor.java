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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import cointoss.Execution;
import cointoss.Market;
import cointoss.Side;
import cointoss.Trading;
import eu.verdelhan.ta4j.Decimal;
import kiss.I;

/**
 * @version 2017/09/07 12:02:46
 */
public class BitFlyerBTCFXMonitor {

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n'Z'");

    private static final ZoneId zone = ZoneId.of("Asia/Tokyo");

    /** The target market */
    private final Market market = new Market(new BitFlyerBTCFX(), null, TradingMonitor.class);

    /**
     * 
     */
    private void start() {
        PNConfiguration config = new PNConfiguration();
        config.setSubscribeKey("sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f");

        PubNub pubNub = new PubNub(config);
        pubNub.addListener(new ExecutionMonitor());
        pubNub.subscribe().channels(I.list("lightning_executions_FX_BTC_JPY")).execute();
    }

    /**
     * @version 2017/09/07 12:35:08
     */
    private static class TradingMonitor extends Trading {

        /**
         * @param market
         */
        public TradingMonitor(Market market) {
            super(market);

            // market.observeExecutionBySize(1).to(exe -> {
            // });

            market.minute1.to(tick -> {
                System.out.println(tick);
            });

            market.minute5.to(tick -> {
                System.out.println(tick);
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryEntry(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tryExit(Execution exe) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void timeline(Execution exe) {
        }
    }

    /**
     * @version 2017/09/07 12:33:49
     */
    private class ExecutionMonitor extends SubscribeCallback {

        /**
         * {@inheritDoc}
         */
        @Override
        public void status(PubNub pubnub, PNStatus status) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void message(PubNub pubnub, PNMessageResult message) {
            if (message.getChannel() != null) {
                Iterator<JsonNode> iterator = message.getMessage().iterator();

                while (iterator.hasNext()) {
                    JsonNode node = iterator.next();

                    Execution exe = new Execution();
                    exe.id = node.get("id").asLong();
                    exe.side = Side.parse(node.get("side").asText());
                    exe.price = Decimal.valueOf(node.get("price").asText());
                    exe.size = Decimal.valueOf(node.get("size").asText());
                    exe.exec_date = LocalDateTime.parse(node.get("exec_date").asText(), format).plusHours(9).atZone(zone);
                    exe.buy_child_order_acceptance_id = node.get("buy_child_order_acceptance_id").asText();
                    exe.sell_child_order_acceptance_id = node.get("sell_child_order_acceptance_id").asText();

                    market.tick(exe);
                }
            }
        }

    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        BitFlyerBTCFXMonitor monitor = new BitFlyerBTCFXMonitor();
        monitor.start();
    }
}
