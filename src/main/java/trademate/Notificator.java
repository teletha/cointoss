/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.concurrent.TimeUnit;

import javafx.geometry.Pos;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import kiss.Variable;
import viewtify.Preference;
import viewtify.Switch;
import viewtify.Viewtify;

/**
 * @version 2017/12/28 9:36:20
 */
public class Notificator extends Preference<Notificator> {

    public final Type longTrend = new Type(3);

    public final Type shortTrend = new Type(3);

    public final Type execution = new Type(3);

    public final Type orderFailed = new Type(3);

    public final Type priceSignal = new Type(15);

    /**
     * 
     */
    private Notificator() {
        restore().autoSave();
    }

    /**
     * @version 2017/12/28 9:37:39
     */
    public static class Type {

        /** The duration of displaying notification. */
        public final Variable<Integer> showTime;

        /** Showing notification pane. */
        public final Variable<Boolean> notification = Variable.of(false);

        /** Showing notification pane. */
        public final Variable<Sound> sound = Variable.of(Sound.なし);

        /** The actual signal. */
        private final Switch<String> signal = new Switch();

        /**
         * @param i
         */
        public Type(int showTime) {
            this.showTime = Variable.of(showTime);
            this.signal.expose.buffer(1000, TimeUnit.MILLISECONDS).to(messages -> {

            });
        }

        /**
         * Notify something.
         * 
         * @param message
         */
        public void notify(String message) {
            if (notification.is(true)) {
                Viewtify.inUI(() -> {
                    Notifications.create()
                            .darkStyle()
                            .hideCloseButton()
                            .position(Pos.TOP_RIGHT)
                            .hideAfter(Duration.seconds(showTime.get()))
                            .text(message)
                            .show();
                });
            }
            sound.get().play();
        }
    }
}
