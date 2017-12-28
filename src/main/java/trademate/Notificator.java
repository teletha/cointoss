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

import kiss.I;
import kiss.Variable;
import viewtify.Preference;

/**
 * @version 2017/12/28 9:36:20
 */
public class Notificator extends Preference<Notificator> {

    public final Kind longTrend = new Kind(3);

    public final Kind shortTrend = new Kind(3);

    public final Kind execution = new Kind(3);

    public final Kind orderFailed = new Kind(3);

    /**
     * 
     */
    private Notificator() {
        restore();
    }

    public static void main(String[] args) throws Exception {
        Notificator make = I.make(Notificator.class);
        make.autoSave();
        System.out.println("start");
        make.execution.notification.set(false);
        Thread.sleep(5000);
    }

    /**
     * @version 2017/12/28 9:37:39
     */
    public static class Kind {

        /** The duration of displaying notification. */
        public final Variable<Integer> showTime;

        /** Showing notification pane. */
        public final Variable<Boolean> notification = Variable.of(true);

        /**
         * @param i
         */
        public Kind(int showTime) {
            this.showTime = Variable.of(showTime);
        }

        /**
         * Notify something.
         * 
         * @param message
         */
        public void notify(String message) {
            if (notification.is(true)) {
                Notifications.create()
                        .darkStyle()
                        .hideCloseButton()
                        .position(Pos.TOP_RIGHT)
                        .hideAfter(Duration.seconds(showTime.get()))
                        .title("OK")
                        .text(message)
                        .show();
            }
        }
    }
}
