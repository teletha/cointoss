/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import static transcript.Transcript.*;

import org.controlsfx.control.Notifications;

import cointoss.util.Network;
import javafx.geometry.Pos;
import javafx.util.Duration;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;
import transcript.Transcript;

@Manageable(lifestyle = Singleton.class)
public class Notificator implements Storable<Notificator> {

    public final Notify longTrend = new Notify(en("Long Trend"));

    public final Notify shortTrend = new Notify(en("Short Trend"));

    public final Notify execution = new Notify(en("Execution"));

    public final Notify orderFailed = new Notify(en("Order Failed"));

    public final Notify priceSignal = new Notify(en("Price Signal"));

    /** The desktop position. */
    public final Variable<DesktopPosition> desktopPosition = Variable.of(DesktopPosition.BottomRight);

    /** The duration of desktop display. */
    public final Variable<java.time.Duration> desktopDuration = Variable.of(java.time.Duration.ofSeconds(1));

    /** The access token for LINE. */
    public final Variable<String> lineAccessToken = Variable.empty();

    /**
     * 
     */
    private Notificator() {
        restore().auto();
    }

    /**
     * 
     */
    public class Notify {

        /** Showing desktop notification. */
        public final Variable<Boolean> onDesktop = Variable.of(false);

        /** Showing line notification. */
        public final Variable<Boolean> onLine = Variable.of(false);

        /** Notifiy by sound. */
        public final Variable<Sound> onSound = Variable.of(Sound.なし);

        /** The name. */
        final Transcript name;

        /**
         * 
         */
        Notify(Transcript name) {
            this.name = name;
        }

        /**
         * Notify.
         * 
         * @param message
         */
        public final void notify(String message) {
            message(message);
            sound();
        }

        /**
         * Notify by simple message.
         * 
         * @param message
         */
        public final void message(String message) {
            if (message != null) {
                message = message.strip();

                if (message.length() != 0) {
                    // to desktop
                    if (onDesktop.is(true)) {
                        Notifications.create()
                                .darkStyle()
                                .hideCloseButton()
                                .position(desktopPosition.v.position)
                                .hideAfter(Duration.seconds(desktopDuration.v.getSeconds()))
                                .text(message)
                                .show();
                    }

                    // to LINE
                    if (onLine.is(true)) {
                        I.make(Network.class).line(message).to(I.NoOP);
                    }
                }
            }
        }

        /**
         * Notify by sound.
         */
        public final void sound() {
            onSound.to(v -> {
                if (v != Sound.なし) {
                    v.play();
                }
            });
        }
    }

    /**
     * 
     */
    static enum DesktopPosition {
        TopLeft(Pos.TOP_LEFT, en("TopLeft")),

        TopRight(Pos.TOP_RIGHT, en("TopRight")),

        BottomLeft(Pos.BOTTOM_LEFT, en("BottomLeft")),

        BottomRight(Pos.BOTTOM_RIGHT, en("BottomRight"));

        /** The actual position. */
        private final Pos position;

        /** The readable text. */
        private final Transcript text;

        /**
         * @param position
         */
        private DesktopPosition(Pos position, Transcript text) {
            this.position = position;
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return text.toString();
        }
    }
}
