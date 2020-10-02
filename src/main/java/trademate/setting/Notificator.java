/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import java.util.List;
import java.util.Objects;

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

@Managed(Singleton.class)
public class Notificator implements Storable<Notificator> {

    /** The defined type. */
    public final Notify longTrend = new Notify(I.translate("Long Trend"));

    /** The defined type. */
    public final Notify shortTrend = new Notify(I.translate("Short Trend"));

    /** The defined type. */
    public final Notify execution = new Notify(I.translate("Execution"));

    /** The defined type. */
    public final Notify orderFailed = new Notify(I.translate("Order Failed"));

    /** The defined type. */
    public final Notify priceSignal = new Notify(I.translate("Price Signal"));

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
    public static class Notify {

        /** Showing desktop notification. */
        final @Managed Variable<Boolean> onDesktop = Variable.of(false);

        /** Showing line notification. */
        final @Managed Variable<Boolean> onLine = Variable.of(false);

        /** Notifiy by sound. */
        final @Managed Variable<Sound> onSound = Variable.of(Sound.なし);

        /** The name. */
        final Variable<String> name;

        /**
         * 
         */
        Notify(Variable<String> name) {
            this.name = name;
        }

        /**
         * Notify by simple message and sound.
         * 
         * @param title A message title.
         * @param message A message.
         */
        public final void notify(Object title, Object message) {
            message(title, message);
            sound();
        }

        /**
         * Notify by simple message.
         * 
         * @param message
         */
        public final void message(Object title, Object message) {
            if (title != null && message != null) {
                String stripedTitle = Objects.toString(title).strip();
                String stripedMessage = Objects.toString(message).strip();
                if (stripedMessage.length() != 0) {
                    Notificator notificator = I.make(Notificator.class);

                    // to desktop
                    if (onDesktop.is(true)) {
                        Notifications.create()
                                .darkStyle()
                                .title(stripedTitle)
                                .text(stripedMessage)
                                .position(notificator.desktopPosition.v.position)
                                .hideAfter(Duration.seconds(notificator.desktopDuration.v.getSeconds()))
                                .hideCloseButton()
                                .owner(Screen.getPrimary())
                                .onAction(e -> {
                                    // hide on click
                                })
                                .show();
                    }

                    // to LINE
                    if (onLine.is(true)) {
                        Network.line(stripedTitle, stripedMessage, notificator.lineAccessToken.v).to(I.NoOP);
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
        TopLeft(Pos.TOP_LEFT, I.translate("TopLeft")),

        TopRight(Pos.TOP_RIGHT, I.translate("TopRight")),

        BottomLeft(Pos.BOTTOM_LEFT, I.translate("BottomLeft")),

        BottomRight(Pos.BOTTOM_RIGHT, I.translate("BottomRight"));

        /** The actual position. */
        private final Pos position;

        /** The readable text. */
        private final Variable<String> text;

        /**
         * @param position
         */
        private DesktopPosition(Pos position, Variable<String> text) {
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

    public static void main(String[] args) throws InterruptedException {
        Notificator n = I.make(Notificator.class);
        n.lineAccessToken.set("ok");

        Thread.sleep(10000);
    }
}