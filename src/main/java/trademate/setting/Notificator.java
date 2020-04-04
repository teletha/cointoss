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

import static transcript.Transcript.en;

import java.util.List;

import javafx.geometry.Pos;
import javafx.stage.Screen;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import kiss.Variable;
import transcript.Transcript;

@Managed(Singleton.class)
public class Notificator implements Storable<Notificator> {

    /** The defined type. */
    public final Notify longTrend = new Notify(en("Long Trend"));

    /** The defined type. */
    public final Notify shortTrend = new Notify(en("Short Trend"));

    /** The defined type. */
    public final Notify execution = new Notify(en("Execution"));

    /** The defined type. */
    public final Notify orderFailed = new Notify(en("Order Failed"));

    /** The defined type. */
    public final Notify priceSignal = new Notify(en("Price Signal"));

    /** The desktop position. */
    final @Managed Variable<DesktopPosition> desktopPosition = Variable.of(DesktopPosition.BottomRight);

    /** The duration of desktop display. */
    final @Managed Variable<java.time.Duration> desktopDuration = Variable.of(java.time.Duration.ofSeconds(1));

    /** The access token for LINE. */
    final @Managed Variable<String> lineAccessToken = Variable.of("");

    /**
     * 
     */
    private Notificator() {
        restore().auto();
    }

    /**
     * Retrieve all notify types.
     * 
     * @return
     */
    List<Notify> types() {
        return I.signal(Notificator.class.getFields()).map(f -> f.get(this)).as(Notify.class).toList();
    }

    /**
     * 
     */
    public class Notify {

        /** Showing desktop notification. */
        final @Managed Variable<Boolean> onDesktop = Variable.of(false);

        /** Showing line notification. */
        final @Managed Variable<Boolean> onLine = Variable.of(false);

        /** Notifiy by sound. */
        final @Managed Variable<Sound> onSound = Variable.of(Sound.なし);

        /** The name. */
        final Transcript name;

        /**
         * 
         */
        Notify(Transcript name) {
            this.name = name;
        }

        /**
         * Notify by simple message and sound.
         * 
         * @param title A message title.
         * @param message A message.
         */
        public final void notify(CharSequence title, CharSequence message) {
            message(title, message);
            sound();
        }

        /**
         * Notify by simple message.
         * 
         * @param message
         */
        public final void message(CharSequence title, CharSequence message) {
            if (title != null && message != null) {
                String stripedTitle = title.toString().strip();
                String stripedMessage = message.toString().strip();
                if (stripedMessage.length() != 0) {
                    // to desktop
                    if (onDesktop.is(true)) {
                        Notifications.create()
                                .darkStyle()
                                .title(stripedTitle)
                                .text(stripedMessage)
                                .position(desktopPosition.v.position)
                                .hideAfter(Duration.seconds(desktopDuration.v.getSeconds()))
                                .hideCloseButton()
                                .owner(Screen.getPrimary())
                                .onAction(e -> {
                                    // hide on click
                                })
                                .show();
                    }

                    // to LINE
                    if (onLine.is(true)) {
                        I.make(Network.class).line(stripedTitle, stripedMessage, lineAccessToken.v).to(I.NoOP);
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
