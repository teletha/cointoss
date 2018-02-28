/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import com.google.gson.JsonElement;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import kiss.I;
import kiss.Signal;

/**
 * @version 2018/02/25 18:36:37
 */
public class Network {

    /**
     * Create pubnub connection.
     * 
     * @param channelName
     * @param subscribeKey
     * @return
     */
    public static Signal<JsonElement> pubnub(String channelName, String subscribeKey) {
        return new Signal<>((observer, disposer) -> {
            PNConfiguration config = new PNConfiguration();
            config.setSecure(true);
            config.setReconnectionPolicy(PNReconnectionPolicy.EXPONENTIAL);
            config.setNonSubscribeRequestTimeout(5);
            config.setPresenceTimeout(5);
            config.setSubscribeTimeout(15);
            config.setStartSubscriberThread(true);
            config.setSubscribeKey(subscribeKey);

            PubNub pubNub = new PubNub(config);
            pubNub.addListener(new SubscribeCallback() {

                /**
                 * @param pubnub
                 * @param status
                 */
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                    System.out.println(status);
                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                        // internet got lost, do some magic and call reconnect when ready
                        pubnub.disconnect();
                        pubnub.reconnect();
                    } else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                        // do some magic and call reconnect when ready
                        pubnub.disconnect();
                        pubnub.reconnect();
                    }
                }

                /**
                 * @param pubnub
                 * @param presence
                 */
                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                    System.out.println("PRESENCE : " + presence);
                }

                /**
                 * @param pubnub
                 * @param message
                 */
                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    if (message.getChannel() != null) {
                        observer.accept(message.getMessage());
                    }
                }
            });
            pubNub.subscribe().channels(I.list(channelName)).execute();

            return disposer.add(() -> {
                pubNub.unsubscribeAll();
                pubNub.forceDestroy();
            });
        });
    }
}
