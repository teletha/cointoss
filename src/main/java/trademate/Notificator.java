/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import javafx.geometry.Pos;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import kiss.Variable;
import viewtify.Preference;
import viewtify.Viewtify;

/**
 * @version 2017/12/28 9:36:20
 */
public class Notificator extends Preference<Notificator> {

    public final Type longTrend = new Type(3);

    public final Type shortTrend = new Type(3);

    public final Type execution = new Type(3);

    public final Type orderFailed = new Type(3);

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

        /**
         * @param i
         */
        public Type(int showTime) {
            this.showTime = Variable.of(showTime);
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
                            .title("OK")
                            .text(message)
                            .show();
                });
            }
            sound.get().play();
        }
    }
}
