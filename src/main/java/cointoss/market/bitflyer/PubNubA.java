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

import java.util.Arrays;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

/**
 * @version 2017/08/23 8:04:14
 */
public class PubNubA {

    public static void main(String[] args) {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f");
        com.pubnub.api.PubNub pubNub = new com.pubnub.api.PubNub(pnConfiguration);
        pubNub.addListener(new SubscribeCallback() {

            @Override
            public void status(PubNub pubnub, PNStatus status) {

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }

            //
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                if (message.getChannel() != null) {
                    Iterator<JsonNode> iterator = message.getMessage().iterator();

                    while (iterator.hasNext()) {
                        JsonNode node = iterator.next();

                        System.out.println(node);
                    }
                }
            }
        });
        pubNub.subscribe().channels(Arrays.asList("lightning_executions_FX_BTC_JPY")).execute();
    }
}
