/*
 * Copyright (C) 2023 The COINTOSS Development Team
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

import cointoss.util.Network;
import kiss.I;
import kiss.Managed;
import kiss.Variable;
import viewtify.model.Preferences;
import viewtify.ui.toast.Toast;

public class Notificator extends Preferences {

    /** The defined type. */
    public final Notify longTrend = new Notify("Long Trend");

    /** The defined type. */
    public final Notify shortTrend = new Notify("Short Trend");

    /** The defined type. */
    public final Notify execution = new Notify("Execution");

    /** The defined type. */
    public final Notify orderFailed = new Notify("Order Failed");

    /** The defined type. */
    public final Notify priceSignal = new Notify("Price Signal");

    /** The master volume for Sound. */
    final @Managed DoublePreference masterVolume = initialize(100.0);

    /** The access token for LINE. */
    final @Managed Preference<String> lineAccessToken = initialize("");

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
    public static class Notify extends Preferences {

        /** Showing desktop notification. */
        final @Managed Preference<Boolean> onDesktop = initialize(false);

        /** Showing line notification. */
        final @Managed Preference<Boolean> onLine = initialize(false);

        /** Notifiy by sound. */
        final @Managed Preference<Sound> onSound = initialize(Sound.なし);

        /** The name. */
        final Variable<String> name;

        /**
         * 
         */
        Notify(String name) {
            this.name = I.translate(name);
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
                        Toast.show(stripedTitle + " " + stripedMessage);
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
}